package com.colisweb.zio.gdrive.client

import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.cloud.bigquery.{Option => _, _}
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._
import zio.clock.Clock
import zio.{RIO, Schedule, ZIO, ZManaged}

import java.nio.ByteBuffer
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

class BigQueryTableZClient[T](
    authenticator: GoogleAuthenticator,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema,
    retryPolicy: Schedule[Any, Throwable, Any] = RetryPolicies.default
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

  private val retry = new Retry(retryPolicy)

  val tableId: TableId          = TableId.of(datasetName, tableName)
  lazy val storedTable: Table   = bigQuery.getTable(tableId)
  lazy val storedSchema: Schema = storedTable.getDefinition[TableDefinition].getSchema

  def appendRows(data: List[T], allowSchemaUpdate: Boolean): RIO[Clock, Unit] =
    for {
      _ <- maybeUpdateSchema().when(allowSchemaUpdate)
      _ <- uploadData(data)
    } yield ()

  def updateRows(data: List[T], map: Map[String, T => String], conditions: List[WhereCondition]): RIO[Clock, Unit] =
    ZIO
      .foreach(data) { row =>
        val query = updateRowQuery(map.view.mapValues(_(row)).toMap, conditions)
        executeQuery(query, "updateJob")
      }
      .unit

  def deleteRows(conditions: List[WhereCondition]): RIO[Clock, Unit] = {
    val where = conditions.map(_.sql).mkString(" and ")
    val query = s"delete from `$datasetName.$tableName` where $where"
    executeQuery(query, "deleteJob").unit
  }

  def executeQuery(query: String, jobPrefix: String): RIO[Clock, TableResult] = {
    val queryJobConfig = QueryJobConfiguration.newBuilder(query).build()
    val jobId          = JobId.newBuilder().setJob(s"${jobPrefix}_${UUID.randomUUID.toString}").build()

    for {
      result <- retry(bigQuery.query(queryJobConfig, jobId))
      _      <- waitForJob(jobId)
    } yield result
  }

  def maybeUpdateSchema(): RIO[Clock, Unit] = {
    val (removedFields, addedFields) = fieldsDiff(storedSchema, schema)

    retry(storedTable.toBuilder.setDefinition(StandardTableDefinition.of(schema)).build().update())
      .when(removedFields.nonEmpty || addedFields.nonEmpty)
  }

  def getAllRows: RIO[Clock, Iterable[FieldValueList]] =
    executeQuery(s"select * from `$datasetName.$tableName`", "get_all_rows")
      .map(_.iterateAll().asScala)

  // TODO: update fields with numeric values
  private def updateRowQuery(toUpdate: Map[String, String], conditions: List[WhereCondition]): String = {
    val where = conditions.map(_.sql).mkString(" and ")
    val set   = toUpdate.map { case (k, v) => s"$k = '$v'" }.mkString(", ")
    s"update `$datasetName.$tableName` set $set where $where"
  }

  private def uploadData(data: List[T]): RIO[Clock, Unit] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(tableId)
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()

    ZManaged
      .fromAutoCloseable(ZIO(bigQuery.writer(jobId, writeJobConfig)))
      .ensuring(waitForJob(jobId).ignore)
      .use { writer =>
        ZIO.foreach(data) { row =>
          retry(writer.write(ByteBuffer.wrap(s"${row.asJson.noSpaces}\n".getBytes(Charsets.UTF_8))))
        }
      }
      .unit
  }

  private def waitForJob(jobId: JobId): RIO[Clock, Try[Job]] =
    retry(bigQuery.getJob(jobId)).map(job => Try(job.waitFor()))
}
