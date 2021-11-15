package com.colisweb.gdrive.client

import com.colisweb.gdrive.client.bigquery.BigQueryTable
import com.google.cloud.bigquery.{Field, Schema, StandardSQLTypeName}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

final class BigQueryTableTest extends AnyWordSpec with Matchers {

  "BigQueryTable" should {
    "identify which fields are different between two schemas" in {
      val schema             = Sample.schema
      val otherFields        = schema.getFields.asScala.toList
      val expectedAddedField = Field.of("new_field", StandardSQLTypeName.BOOL)
      val otherSchema        = Schema.of((expectedAddedField :: otherFields.tail).asJava)

      BigQueryTable.sameSchemas(schema, otherSchema) should be(false)
    }
  }
}
