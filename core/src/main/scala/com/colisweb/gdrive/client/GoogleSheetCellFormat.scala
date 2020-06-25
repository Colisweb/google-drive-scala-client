package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{CellFormat, NumberFormat, TextFormat}

trait GoogleSheetCellFormat {
  def format: CellFormat
}

case object BoldFormat extends GoogleSheetCellFormat {
  val format: CellFormat = new CellFormat().setTextFormat(new TextFormat().setBold(true))
}

case object PercentageFormat extends GoogleSheetCellFormat {
  val format: CellFormat = new CellFormat().setNumberFormat(new NumberFormat().setType("PERCENT").setPattern("#0.00%"))
}
