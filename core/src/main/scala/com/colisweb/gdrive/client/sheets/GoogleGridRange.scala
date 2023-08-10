package com.colisweb.gdrive.client.sheets

import com.google.api.services.sheets.v4.model.GridRange

final case class GoogleGridRange(
    sheetId: Int,
    row: Option[GoogleDimensionRange] = None,
    column: Option[GoogleDimensionRange] = None
) {
  val toGoogle: GridRange = {
    val gridRange = new GridRange().setSheetId(sheetId)

    val rowGridRange = row.fold(gridRange) {
      case GoogleDimensionRange(i, None)         => gridRange.setStartRowIndex(i)
      case GoogleDimensionRange(i, Some(length)) => gridRange.setStartRowIndex(i).setEndRowIndex(i + length)
    }

    column.fold(rowGridRange) {
      case GoogleDimensionRange(i, None)         => rowGridRange.setStartRowIndex(i)
      case GoogleDimensionRange(i, Some(length)) => rowGridRange.setStartColumnIndex(i).setEndColumnIndex(i + length)
    }
  }
}

object GoogleGridRange {

  def singleRow(sheetId: Int, rowIndex: Int): GoogleGridRange =
    multipleRows(sheetId, rowIndex, 1)

  def singleColumn(sheetId: Int, columnIndex: Int): GoogleGridRange =
    multipleColumns(sheetId, columnIndex, 1)

  def multipleRows(sheetId: Int, rowIndex: Int, length: Int): GoogleGridRange =
    GoogleGridRange(sheetId, row = Some(GoogleDimensionRange(rowIndex, Some(length))))

  def multipleColumns(sheetId: Int, rowIndex: Int, length: Int): GoogleGridRange =
    GoogleGridRange(sheetId, column = Some(GoogleDimensionRange(rowIndex, Some(length))))

}
