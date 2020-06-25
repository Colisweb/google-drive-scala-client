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

  def singleRowGridRange(rowIndex: Int, sheetId: Int): GridRange =
    new GridRange().setSheetId(sheetId).setStartRowIndex(rowIndex).setEndRowIndex(rowIndex + 1)

  def singleColumnGridRange(columnIndex: Int, sheetId: Int): GridRange =
    columnGridRange(columnIndex, sheetId, 1)

  def columnGridRange(columnIndex: Int, sheetId: Int, length: Int): GridRange =
    new GridRange().setSheetId(sheetId).setStartColumnIndex(columnIndex).setEndColumnIndex(columnIndex + length)
}
