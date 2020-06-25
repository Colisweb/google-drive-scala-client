package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model._

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

final case class FormatRange(
    range: GridRange,
    cellFormat: GoogleSheetCellFormat,
    fields: GoogleSheetField
) extends GoogleBatchRequest {
  def request: Request = {
    val cellData          = new CellData().setUserEnteredFormat(cellFormat.format)
    val repeatCellRequest = new RepeatCellRequest().setRange(range).setCell(cellData).setFields(fields.field)

    new Request().setRepeatCell(repeatCellRequest)
  }
}

final case class CopyPaste(source: GridRange, destination: GridRange) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCopyPaste(new CopyPasteRequest().setSource(source).setDestination(destination))
}

final case class CutPaste(source: GridRange, destination: GridCoordinate) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCutPaste(new CutPasteRequest().setSource(source).setDestination(destination))
}
