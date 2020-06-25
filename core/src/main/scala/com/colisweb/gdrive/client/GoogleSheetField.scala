package com.colisweb.gdrive.client

trait GoogleSheetField {
  def field: String
}

case object Number extends GoogleSheetField {
  val field = "userEnteredFormat.numberFormat"
}

case object BoldText extends GoogleSheetField {
  val field = "userEnteredFormat.textFormat.bold"
}

case object BackgroundColor extends GoogleSheetField {
  val field = "userEnteredFormat.backgroundColor"
}

case object Text extends GoogleSheetField {
  val field = "userEnteredFormat.textFormat"
}

case object HorizontalAlignment extends GoogleSheetField {
  val field = "userEnteredFormat.horizontalAlignment"
}

// TODO: Generic method to combine fields & remove
case object TextFormatAndHorizontalAlignment extends GoogleSheetField {
  val field = "userEnteredFormat(textFormat, horizontalAlignment)"
}
