package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{CellFormat, Color, NumberFormat, TextFormat}

trait GoogleSheetCellFormat {
  def combine: CellFormat => CellFormat

  def format: CellFormat = combine(new CellFormat)
}

case object BoldFormat extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setTextFormat(new TextFormat().setBold(true))
}

case object PercentageFormat extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setNumberFormat(new NumberFormat().setType("PERCENT").setPattern("#0.00%"))
}

final case class BackgroundColorFormat(red: Float, green: Float, blue: Float) extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setBackgroundColor(new Color().setRed(red).setGreen(green).setBlue(blue))
}
