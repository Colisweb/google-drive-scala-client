package com.colisweb.gdrive.client

import com.colisweb.gdrive.client.bigquery.BigQuerySchema
import com.google.cloud.bigquery.Schema

import java.time.{Instant, LocalDate, LocalDateTime}

final case class Sample(field1: Int, date: LocalDate, timestamp: Instant, subfields: List[SubSample])

final case class SubSample(string: String, dateTime: LocalDateTime)

object Sample {
  val schema: Schema = BigQuerySchema.schema[Sample]
}
