package com.colisweb.gdrive.client.sheets

import com.colisweb.gdrive.client.sheets.GooglePivotTable.{GooglePivotGroup, GooglePivotValue}
import com.google.api.services.sheets.v4.model.{DataSourceColumnReference, PivotGroup, PivotTable, PivotValue}

import scala.jdk.CollectionConverters._

final case class GooglePivotTable(rows: List[GooglePivotGroup], values: List[GooglePivotValue]) {

  def toGoogle: PivotTable =
    new PivotTable()
      .setRows(rows.map(_.toGoogle).asJava)
      .setValues(values.map(_.toGoogle).asJava)
}

object GooglePivotTable {

  sealed trait GoogleSummarizedFunction { val value: String }

  case object Average extends GoogleSummarizedFunction { override val value = "AVERAGE" }
  case object Counta  extends GoogleSummarizedFunction { override val value = "COUNTA"  }
  case object Stdev   extends GoogleSummarizedFunction { override val value = "STDEV"   }

  final case class GooglePivotGroup(columnReference: String, sortOrder: String) {

    def toGoogle: PivotGroup =
      new PivotGroup()
        .setDataSourceColumnReference(new DataSourceColumnReference().setName(columnReference))
        .setSortOrder(sortOrder)
  }

  final case class GooglePivotValue(
      name: String,
      columnReference: String,
      summarizedFunction: GoogleSummarizedFunction
  ) {

    def toGoogle: PivotValue =
      new PivotValue()
        .setDataSourceColumnReference(new DataSourceColumnReference().setName(columnReference))
        .setSummarizeFunction(summarizedFunction.value)
        .setName(name)
  }
}
