package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model._

import scala.jdk.CollectionConverters._

object GoogleUtilities {

  def asRowData(data: List[String], format: CellFormat): RowData =
    new RowData().setValues(
      data.map { d =>
        new CellData()
          .setUserEnteredValue(new ExtendedValue().setStringValue(d))
          .setEffectiveFormat(format)
      }.asJava
    )

  implicit class GoogleListData[A](data: java.util.List[A]) {
    def asScalaListNotNull: List[A] = data match {
      case null => Nil
      case _    => data.asScala.toList
    }
  }
}
