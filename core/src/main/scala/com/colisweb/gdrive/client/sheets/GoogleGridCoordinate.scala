package com.colisweb.gdrive.client.sheets

import com.google.api.services.sheets.v4.model.GridCoordinate

final case class GoogleGridCoordinate(sheetId: Int, rowIndex: Int, columnIndex: Int) {
  val toGoogle: GridCoordinate =
    new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex).setColumnIndex(columnIndex)
}

object GoogleGridCoordinate {
  def zero(sheetId: Int): GoogleGridCoordinate = GoogleGridCoordinate(sheetId, 0, 0)
}
