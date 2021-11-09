package com.colisweb.cats.gdrive.client

import cats.effect.{Resource, Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.bigquery.BigQueryTable._
import com.google.cloud.bigquery.{Option => _, _}
import com.google.common.base.Charsets
import io.circe.Encoder
import io.circe.syntax._
import retry.{RetryDetails, RetryPolicy}

import java.nio.ByteBuffer
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

class BigQueryTableSync[F[_], T](
    authenticator: GoogleAuthenticator,
    projectId: String,
    datasetName: String,
    tableName: String,
    schema: Schema,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit F: Sync[F], timer: Timer[F], encoder: Encoder[T])
    extends Retry[F](retryPolicy, onError) {

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

  def appendRows(data: List[T], allowSchemaUpdate: Boolean): F[Unit] =
    for {
      _ <- if (allowSchemaUpdate) maybeUpdateSchema() else F.unit
      _ <- uploadData(data)
    } yield ()

  def updateRows(data: List[T], map: Map[String, T => String], conditions: List[WhereCondition]): F[Unit] =
    data.traverse { row =>
      val query = updateRowQuery(map.view.mapValues(_(row)).toMap, conditions)
      executeQuery(query, "updateJob")
    }.void

  def deleteRows(conditions: List[WhereCondition]): F[Unit] = {
    val where = conditions.map(_.sql).mkString(" and ")
    val query = s"delete from `$datasetName.$tableName` where $where"
    executeQuery(query, "deleteJob").void
  }

  def executeQuery(query: String, jobPrefix: String): F[TableResult] = {
    val queryJobConfig = QueryJobConfiguration.newBuilder(query).build()
    val jobId          = JobId.newBuilder().setJob(s"${jobPrefix}_${UUID.randomUUID.toString}").build()

    for {
      result <- retry(bigQuery.query(queryJobConfig, jobId))
      _      <- waitForJob(jobId)
    } yield result
  }

  def maybeUpdateSchema(): F[Unit] = {
    val (removedFields, addedFields) = fieldsDiff(storedSchema, schema)

    if (removedFields.nonEmpty || addedFields.nonEmpty)
      retry(storedTable.toBuilder.setDefinition(StandardTableDefinition.of(schema)).build().update()).void
    else
      F.unit
  }

  def getAllRows: F[Iterable[FieldValueList]] =
    executeQuery(s"select * from `$datasetName.$tableName`", "get_all_rows")
      .map(_.iterateAll().asScala)

  // TODO: update fields with numeric values
  private def updateRowQuery(toUpdate: Map[String, String], conditions: List[WhereCondition]): String = {
    val where = conditions.map(_.sql).mkString(" and ")
    val set   = toUpdate.map { case (k, v) => s"$k = '$v'" }.mkString(", ")
    s"update `$datasetName.$tableName` set $set where $where"
  }

  private def uploadData(data: List[T]): F[Unit] = {
    val writeJobConfig =
      WriteChannelConfiguration
        .newBuilder(tableId)
        .setFormatOptions(FormatOptions.json())
        .setSchema(schema)
        .build()

    val jobId = JobId.newBuilder().setJob(s"appendJob_${UUID.randomUUID.toString}").build()

    Resource
      .fromAutoCloseable(F.delay(bigQuery.writer(jobId, writeJobConfig)))
      .onFinalize(waitForJob(jobId).void)
      .use { writer =>
        data.traverse(d => retry(writer.write(ByteBuffer.wrap(s"${d.asJson.noSpaces}\n".getBytes(Charsets.UTF_8)))))
      }
      .void
  }

  private def waitForJob(jobId: JobId): F[Try[Job]] =
    retry(bigQuery.getJob(jobId)).map(job => Try(job.waitFor()))
}
