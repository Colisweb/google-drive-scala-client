package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{GridCoordinate, GridRange}

final case class GoogleDimensionRange(index: Int = 0, length: Option[Int] = None)

final case class GoogleGridCoordinate(sheetId: Int, rowIndex: Int, columnIndex: Int) {
  def toGoogle: GridCoordinate =
    new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex).setColumnIndex(columnIndex)
}

final case class GoogleGridRange(
    sheetId: Int,
    row: GoogleDimensionRange = GoogleDimensionRange(),
    col: GoogleDimensionRange = GoogleDimensionRange()
) {

  def toGoogle: GridRange = {
    val gridRange = new GridRange()
      .setSheetId(sheetId)
      .setStartRowIndex(row.index)
      .setStartColumnIndex(col.index)

    (row.length, col.length) match {
      case (None, None) =>
        gridRange

      case (Some(length), None) =>
        gridRange.setEndRowIndex(row.index + length)

      case (None, Some(length)) =>
        gridRange.setEndColumnIndex(col.index + length)

      case (Some(rowLength), Some(colLength)) =>
        gridRange
          .setEndColumnIndex(col.index + colLength)
          .setEndRowIndex(row.index + rowLength)
    }

  }
}
