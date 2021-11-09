package com.colisweb.gdrive.client

import com.github.writethemfirst.Approbation
import com.google.cloud.bigquery.{Field, Schema}
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

final class BigQuerySchemaTest extends FixtureAnyFlatSpec with Approbation with Matchers {

  "BigQuery schema" should "be valid" in { approver =>
    approver.verify(prettify(getFields(Sample.schema)))
  }

  private def getFields(schema: Schema): List[(String, String, String, Any)] =
    schema.getFields.asScala.toList.map(getField)

  private def getField(field: Field): (String, String, String, Any) =
    (
      field.getName,
      field.getType.getStandardType.name(),
      Option(field.getMode).map(_.name()).getOrElse(""),
      Option(field.getSubFields).map(_.asScala.toList).getOrElse(Nil).map(getField)
    )

}
