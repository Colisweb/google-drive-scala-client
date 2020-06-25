package com.colisweb.gdrive.client

trait GoogleSheetField {
  def field: String
}

case object NumberFormatField extends GoogleSheetField {
  val field = "userEnteredFormat.numberFormat"
}

case object BoldTextField extends GoogleSheetField {
  val field = "userEnteredFormat.textFormat.bold"
}

case object BackgroundColorField extends GoogleSheetField {
  val field = "userEnteredFormat.backgroundColor"
}

case object TextFormatField extends GoogleSheetField {
  val field = "userEnteredFormat.textFormat"
}

case object HorizontalAlignmentField extends GoogleSheetField {
  val field = "userEnteredFormat.horizontalAlignment"
}

// TODO: Generic method to combine fields & remove this
case object TextFormatAndHorizontalAlignmentField extends GoogleSheetField {
  val field = "userEnteredFormat(textFormat, horizontalAlignment)"
}
