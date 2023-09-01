package com.colisweb.gdrive.client.sheets.formatting

import com.google.api.services.sheets.v4.model.{CellFormat, Color, NumberFormat}

// TODO? it's a monoid!
trait GoogleSheetCellFormat {
  def combine: CellFormat => CellFormat

  def format: CellFormat = combine(new CellFormat)
}

object GoogleSheetCellFormat {

  def combineMultipleFormats(formats: List[GoogleSheetCellFormat]): GoogleSheetCellFormat =
    new GoogleSheetCellFormat {
      val combine: CellFormat => CellFormat =
        format => formats.foldLeft(format) { case (acc, next) => next.combine(acc) }
    }

}

final case class DateFormat(format: String) extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setNumberFormat(new NumberFormat().setType("DATE").setPattern(format))
}

case object EuropeanDateFormat extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = DateFormat("dd-mm-yyyy").combine
}

case object PercentageFormat extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setNumberFormat(new NumberFormat().setType("PERCENT").setPattern("#0.00%"))
}

final case class BackgroundColorFormat(red: Float, green: Float, blue: Float) extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setBackgroundColor(new Color().setRed(red).setGreen(green).setBlue(blue))
}

final case class HorizontalAlignmentFormat(alignment: HorizontalAlignment) extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setHorizontalAlignment(alignment.code)
}

final case class CellTextFormat(textFormat: GoogleSheetTextFormat) extends GoogleSheetCellFormat {
  val combine: CellFormat => CellFormat = _.setTextFormat(textFormat.format)
}
