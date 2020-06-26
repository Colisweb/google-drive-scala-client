package com.colisweb.gdrive.client.sheets

import com.colisweb.gdrive.client.sheets.formatting.{GoogleSheetCellFormat, GoogleSheetField}
import com.google.api.services.sheets.v4.model._

trait GoogleBatchRequest {
  def request: Request
}

final case class AutoResize(id: String, sheetId: Int, dimension: GoogleSheetDimension) extends GoogleBatchRequest {
  def request: Request = {
    val dimensionsRange = new DimensionRange().setSheetId(sheetId).setDimension(dimension.code)
    val resizeRequest   = new AutoResizeDimensionsRequest().setDimensions(dimensionsRange)

    new Request().setAutoResizeDimensions(resizeRequest)
  }
}

final case class FormatRange(
    range: GoogleGridRange,
    cellFormat: GoogleSheetCellFormat,
    fields: GoogleSheetField
) extends GoogleBatchRequest {
  def request: Request = {
    val cellData          = new CellData().setUserEnteredFormat(cellFormat.format)
    val repeatCellRequest = new RepeatCellRequest().setRange(range.toGoogle).setCell(cellData).setFields(fields.field)

    new Request().setRepeatCell(repeatCellRequest)
  }
}

final case class CopyPaste(source: GoogleGridRange, destination: GoogleGridRange) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCopyPaste(new CopyPasteRequest().setSource(source.toGoogle).setDestination(destination.toGoogle))
}

final case class CutPaste(source: GoogleGridRange, destination: GoogleGridCoordinate) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCutPaste(new CutPasteRequest().setSource(source.toGoogle).setDestination(destination.toGoogle))
}

final case class AppendDimension(
    sheetId: Int,
    dimension: GoogleSheetDimension,
    length: Int
) extends GoogleBatchRequest {
  def request: Request =
    new Request().setAppendDimension(
      new AppendDimensionRequest().setSheetId(sheetId).setDimension(dimension.code).setLength(length)
    )
}

final case class InsertDimension(
    sheetId: Int,
    dimension: GoogleSheetDimension,
    startIndex: Int,
    endIndex: Int
) extends GoogleBatchRequest {
  def request: Request = {
    val dimensionRange = new DimensionRange()
      .setSheetId(sheetId)
      .setStartIndex(startIndex)
      .setEndIndex(endIndex)
      .setDimension(dimension.code)

    new Request().setInsertDimension(new InsertDimensionRequest().setRange(dimensionRange))
  }
}
