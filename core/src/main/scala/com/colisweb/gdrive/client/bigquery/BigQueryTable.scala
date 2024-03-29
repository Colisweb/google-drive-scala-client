package com.colisweb.gdrive.client.bigquery

import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.bigquery.{Option => _, _}
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._

import java.nio.ByteBuffer
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

class BigQueryTable[T](
    credentials: GoogleCredentials,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema
)(implicit encoder: Encoder[T]) {

  val bigQueryService: BigQuery = BigQueryOptions
    .newBuilder()
    .setCredentials(credentials)
    .setProjectId(projectId)
    .build()
    .getService

  val tableId: TableId                  = TableId.of(datasetName, tableName)
  lazy val storedTable: Option[Table]   = Option(bigQueryService.getTable(tableId))
  lazy val storedSchema: Option[Schema] = storedTable.map(_.getDefinition[TableDefinition].getSchema)

  def appendRows(data: Iterable[T], allowSchemaUpdate: Boolean): Try[Job] = {
    if (allowSchemaUpdate) maybeUpdateSchema()
    uploadData(data)
  }

  def updateRows(
      data: Iterable[T],
      fieldsToUpdate: Map[String, T => String],
      conditions: Iterable[WhereCondition]
  ): Unit =
    data.foreach(updateRow(fieldsToUpdate, conditions))

  def updateRow(fieldsToUpdate: Map[String, T => String], conditions: Iterable[WhereCondition])(row: T): TableResult = {
    val toUpdate = fieldsToUpdate.map { case (colName, value) => (colName, value(row)) }.toMap
    val query    = updateRowQuery(toUpdate, conditions)
    executeQuery(query, "updateJob")
  }

  def deleteRows(conditions: Iterable[WhereCondition]): TableResult = {
    val where = conditions.map(_.sql).mkString(" and ")
    val query = s"delete from `$datasetName.$tableName` where $where"
    executeQuery(query, "deleteJob")
  }

  def executeQuery(query: String, jobPrefix: String): TableResult = {
    val queryJobConfig = QueryJobConfiguration.newBuilder(query).build()
    val jobId          = JobId.newBuilder().setJob(s"${jobPrefix}_${UUID.randomUUID.toString}").build()
    val result         = bigQueryService.query(queryJobConfig, jobId)
    waitForJob(jobId)
    result
  }

  def maybeUpdateSchema(): Unit =
    (storedTable, storedSchema) match {
      case (Some(remoteTable), Some(remoteSchema)) if !sameSchemas(remoteSchema, schema) =>
        remoteTable.toBuilder.setDefinition(StandardTableDefinition.of(schema)).build().update()
        ()

      case _ => ()
    }

  def getAllRows: Iterable[FieldValueList] =
    executeQuery(s"select * from `$datasetName.$tableName`", "get_all_rows")
      .iterateAll()
      .asScala

  def waitForJob(jobId: JobId): Try[Job] =
    Try(bigQueryService.getJob(jobId).waitFor())

  // TODO: change fieldsToUpdate to be able to update fields with numeric values (here we only update with strings)
  def updateRowQuery(fieldsToUpdate: Map[String, String], conditions: Iterable[WhereCondition]): String = {
    val where = conditions.map(_.sql).mkString(" and ")
    val set   = fieldsToUpdate.map { case (col, value) => s"$col = '$value'" }.mkString(", ")
    s"update `$datasetName.$tableName` set $set where $where"
  }

  def uploadData(data: Iterable[T]): Try[Job] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(tableId)
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId  = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()
    val writer = bigQueryService.writer(jobId, writeJobConfig)

    data.foreach(d => writer.write(ByteBuffer.wrap(s"${d.asJson.noSpaces}\n".getBytes(Charsets.UTF_8))))
    writer.close()
    waitForJob(jobId)
  }
}

object BigQueryTable {
  implicit val doubleEncoder: Encoder[Double] = Encoder.encodeDouble.contramap { double =>
    if (double.isNaN || double.isInfinite)
      double
    else
      BigDecimal(double).setScale(9, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  sealed trait WhereCondition {
    def sql: String
  }

  final case class WhereConditionValue(columnName: String, requiredValue: String) extends WhereCondition {
    def sql: String = s"$columnName = '$requiredValue'"
  }

  final case class WhereConditionNumericValue(columnName: String, requiredValue: Double) extends WhereCondition {
    def sql: String = s"$columnName = $requiredValue"
  }

  final case class WhereConditionList(columnName: String, values: Iterable[String]) extends WhereCondition {
    def sql: String = s"$columnName in (${values.map(v => s"'$v'").mkString(", ")})"
  }

  def sameSchemas(left: Schema, right: Schema): Boolean =
    left.getFields.asScala.toSet == right.getFields.asScala.toSet
}
