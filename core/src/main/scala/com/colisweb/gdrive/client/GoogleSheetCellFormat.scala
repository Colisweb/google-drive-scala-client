package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{CellFormat, NumberFormat, TextFormat}

trait GoogleSheetCellFormat {
  def format: CellFormat
}

case object Bold extends GoogleSheetCellFormat {
  val format: CellFormat = new CellFormat().setTextFormat(new TextFormat().setBold(true))
}

case object Percentage extends GoogleSheetCellFormat {
  val format: CellFormat = new CellFormat().setNumberFormat(new NumberFormat().setType("PERCENT").setPattern("#0.00%"))
}
