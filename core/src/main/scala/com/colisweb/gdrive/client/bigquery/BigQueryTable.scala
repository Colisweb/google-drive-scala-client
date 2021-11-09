package com.colisweb.gdrive.client.bigquery

import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.cloud.bigquery._
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._

import java.nio.ByteBuffer
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

class BigQueryTable[T](
    authenticator: GoogleAuthenticator,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema
)(implicit encoder: Encoder[T]) {

  private val bigQuery = BigQueryOptions
    .newBuilder()
    .setCredentials(authenticator.credentials)
    .setProjectId(projectId)
    .build()
    .getService

  implicit val doubleEncoder: Encoder[Double] = Encoder.encodeDouble.contramap { double =>
    if (double.isNaN || double.isInfinite)
      double
    else
      BigDecimal(double).setScale(9, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  val tableId: TableId          = TableId.of(datasetName, tableName)
  lazy val storedTable: Table   = bigQuery.getTable(tableId)
  lazy val storedSchema: Schema = storedTable.getDefinition[TableDefinition].getSchema

  def appendRows(data: List[T], allowSchemaUpdate: Boolean): Try[Job] = {
    if (allowSchemaUpdate) maybeUpdateSchema() else ()
    uploadData(data)
  }

  def updateRows(data: List[T], map: Map[String, T => String], conditions: List[WhereCondition]): Unit =
    data.foreach { row =>
      val query = updateRowQuery(map.view.mapValues(_(row)).toMap, conditions)
      executeQuery(query, "updateJob")
    }

  def deleteRows(conditions: List[WhereCondition]): TableResult = {
    val where = conditions.map(_.sql).mkString(" and ")
    val query = s"delete from `$datasetName.$tableName` where $where"
    executeQuery(query, "deleteJob")
  }

  def executeQuery(query: String, jobPrefix: String): TableResult = {
    val queryJobConfig = QueryJobConfiguration.newBuilder(query).build()
    val jobId          = JobId.newBuilder().setJob(s"${jobPrefix}_${UUID.randomUUID.toString}").build()
    val result         = bigQuery.query(queryJobConfig, jobId)
    waitForJob(jobId)
    result
  }

  def maybeUpdateSchema(): Unit = {
    val (removedFields, addedFields) = BigQueryTable.fieldsDiff(storedSchema, schema)

    if (removedFields.nonEmpty || addedFields.nonEmpty)
      storedTable.toBuilder.setDefinition(StandardTableDefinition.of(schema)).build().update()

    ()
  }

  def getAllRows: Iterable[FieldValueList] =
    executeQuery(s"select * from `$datasetName.$tableName`", "get_all_rows")
      .iterateAll()
      .asScala

  // TODO: update fields with numeric values
  private def updateRowQuery(toUpdate: Map[String, String], conditions: List[WhereCondition]): String = {
    val where = conditions.map(_.sql).mkString(" and ")
    val set   = toUpdate.map { case (k, v) => s"$k = '$v'" }.mkString(", ")
    s"update `$datasetName.$tableName` set $set where $where"
  }

  private def uploadData(data: List[T]): Try[Job] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(tableId)
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId  = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()
    val writer = bigQuery.writer(jobId, writeJobConfig)
    data.foreach(d => writer.write(ByteBuffer.wrap(s"${d.asJson.noSpaces}\n".getBytes(Charsets.UTF_8))))
    waitForJob(jobId)
  }

  private def waitForJob(jobId: JobId): Try[Job] =
    Try(bigQuery.getJob(jobId).waitFor())
}

object BigQueryTable {
  sealed trait WhereCondition {
    def sql: String
  }

  final case class WhereConditionValue(columnName: String, requiredValue: String) extends WhereCondition {
    def sql: String = s"$columnName = '$requiredValue'"
  }

  final case class WhereConditionNumericValue(columnName: String, requiredValue: Double) extends WhereCondition {
    def sql: String = s"$columnName = $requiredValue"
  }

  final case class WhereConditionList(columnName: String, values: List[String]) extends WhereCondition {
    def sql: String = s"$columnName in (${values.map(v => s"'$v''").mkString(", ")})"
  }

  def fieldsDiff(left: Schema, right: Schema): (List[Field], List[Field]) = {
    val leftFields  = left.getFields.asScala.toList
    val rightFields = right.getFields.asScala.toList

    (leftFields diff rightFields, rightFields diff leftFields)
  }
}
