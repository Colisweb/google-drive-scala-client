package com.colisweb.cats.gdrive.client

import cats.effect.{Resource, Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.bigquery.BigQueryTable
import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.cloud.bigquery.{Option => _, _}
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._
import retry.{RetryDetails, RetryPolicy}

import java.nio.ByteBuffer
import java.util.UUID
import scala.util.Try

class BigQueryTableRetry[F[_], T](
    authenticator: GoogleAuthenticator,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit F: Sync[F], timer: Timer[F], encoder: Encoder[T])
    extends Retry[F](retryPolicy, onError) {

  val bigQueryTable =
    new BigQueryTable(authenticator.credentials, projectId, datasetName, tableName, schema)(encoder)

  lazy val storedTable: F[Option[Table]]   = retry(bigQueryTable.storedTable)
  lazy val storedSchema: F[Option[Schema]] = retry(bigQueryTable.storedSchema)

  def appendRows(data: Iterable[T], allowSchemaUpdate: Boolean): F[Unit] =
    F.whenA(allowSchemaUpdate)(maybeUpdateSchema()) *> uploadData(data)

  def updateRows(
      data: Iterable[T],
      fieldsToUpdate: Map[String, T => String],
      conditions: Iterable[WhereCondition]
  ): F[Unit] =
    data.toVector.traverse(row => retry(bigQueryTable.updateRow(fieldsToUpdate, conditions)(row))).void

  def deleteRows(conditions: Iterable[WhereCondition]): F[Unit] =
    retry(bigQueryTable.deleteRows(conditions)).void

  def executeQuery(query: String, jobPrefix: String): F[TableResult] =
    retry(bigQueryTable.executeQuery(query, jobPrefix))

  def maybeUpdateSchema(): F[Unit] =
    retry(bigQueryTable.maybeUpdateSchema())

  def getAllRows: F[Iterable[FieldValueList]] =
    retry(bigQueryTable.getAllRows)

  def waitForJob(jobId: JobId): F[Try[Job]] =
    retry(bigQueryTable.waitForJob(jobId))

  def uploadData(data: Iterable[T]): F[Unit] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(TableId.of(datasetName, tableName))
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()

    Resource
      .fromAutoCloseable(F.delay(bigQueryTable.bigQueryService.writer(jobId, writeJobConfig)))
      .onFinalize(waitForJob(jobId).void)
      .use { writer =>
        data.toVector.traverse(d =>
          retry(writer.write(ByteBuffer.wrap(s"${d.asJson.noSpaces}\n".getBytes(Charsets.UTF_8))))
        )
      }
      .void
  }
}
