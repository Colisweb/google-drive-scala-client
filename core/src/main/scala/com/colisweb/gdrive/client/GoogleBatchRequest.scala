package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{
  AutoResizeDimensionsRequest,
  CellData,
  CellFormat,
  DimensionRange,
  GridRange,
  RepeatCellRequest,
  Request
}

sealed trait GoogleBatchRequest {
  def request: Request
}

final case class AutoResizeColumns(id: String, sheetId: Int) extends GoogleBatchRequest {
  def request: Request = {
    val dimensionsRange = new DimensionRange().setSheetId(sheetId).setDimension("COLUMNS")
    val resizeRequest   = new AutoResizeDimensionsRequest().setDimensions(dimensionsRange)

    new Request().setAutoResizeDimensions(resizeRequest)
  }
}

final case class FormatRange(range: GridRange, format: CellFormat, fields: String) extends GoogleBatchRequest {
  def request: Request = {
    val cellData          = new CellData().setUserEnteredFormat(format)
    val repeatCellRequest = new RepeatCellRequest().setRange(range).setCell(cellData).setFields(fields)

    new Request().setRepeatCell(repeatCellRequest)
  }
}
