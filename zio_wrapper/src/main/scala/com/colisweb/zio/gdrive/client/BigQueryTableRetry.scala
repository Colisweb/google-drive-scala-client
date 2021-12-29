package com.colisweb.zio.gdrive.client

import com.colisweb.gdrive.client.bigquery.BigQueryTable
import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.bigquery.{Option => _, _}
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._
import zio.clock.Clock
import zio.{RIO, Schedule, ZIO, ZManaged}

import java.nio.ByteBuffer
import java.util.UUID
import scala.util.Try

class BigQueryTableRetry[T](
    credentials: GoogleCredentials,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema,
    retryPolicy: Schedule[Any, Throwable, Any] = RetryPolicies.default
)(implicit encoder: Encoder[T]) {

  private val bigQueryTable = new BigQueryTable(credentials, projectId, datasetName, tableName, schema)(encoder)
  private val retry         = new Retry(retryPolicy)

  lazy val storedTable: RIO[Clock, Option[Table]]   = retry(bigQueryTable.storedTable)
  lazy val storedSchema: RIO[Clock, Option[Schema]] = retry(bigQueryTable.storedSchema)

  def appendRows(data: List[T], allowSchemaUpdate: Boolean): RIO[Clock, Unit] =
    (maybeUpdateSchema().when(allowSchemaUpdate)
      *> uploadData(data))

  def updateRows(
      data: List[T],
      fieldsToUpdate: Map[String, T => String],
      conditions: List[WhereCondition]
  ): RIO[Clock, Unit] =
    ZIO
      .foreach(data)(row => retry(bigQueryTable.updateRow(fieldsToUpdate, conditions)(row)))
      .unit

  def deleteRows(conditions: List[WhereCondition]): RIO[Clock, Unit] =
    retry(bigQueryTable.deleteRows(conditions)).unit

  def executeQuery(query: String, jobPrefix: String): RIO[Clock, TableResult] =
    retry(bigQueryTable.executeQuery(query, jobPrefix))

  def maybeUpdateSchema(): RIO[Clock, Unit] =
    retry(bigQueryTable.maybeUpdateSchema())

  def getAllRows: RIO[Clock, Iterable[FieldValueList]] =
    retry(bigQueryTable.getAllRows)

  private def waitForJob(jobId: JobId): RIO[Clock, Try[Job]] =
    retry(bigQueryTable.waitForJob(jobId))

  def uploadData(data: List[T]): RIO[Clock, Unit] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(TableId.of(datasetName, tableName))
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()

    ZManaged
      .fromAutoCloseable(ZIO(bigQueryTable.bigQueryService.writer(jobId, writeJobConfig)))
      .ensuring(waitForJob(jobId).ignore)
      .use { writer =>
        ZIO.foreach(data) { row =>
          retry(writer.write(ByteBuffer.wrap(s"${row.asJson.noSpaces}\n".getBytes(Charsets.UTF_8))))
        }
      }
      .unit
  }
}
