package com.colisweb.gdrive.client.sheets

import com.google.api.services.sheets.v4.model.{DataSourceColumnReference, DataSourceTable}

import scala.jdk.CollectionConverters._

final case class GoogleDataSourceTable(
    columnsNames: List[String],
    rowLimit: Int = 1000,
    dataSourceId: Option[String] = None
) {
  def toGoogle: DataSourceTable =
    new DataSourceTable()
      .setColumns(columnsNames.map { name =>
        val ref = new DataSourceColumnReference()
        ref.setName(name)
      }.asJava)
      .setRowLimit(rowLimit)
      .setDataSourceId(dataSourceId.orNull)
}
