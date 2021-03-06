package com.colisweb.gdrive.client.sheets

sealed trait GoogleSheetDimension {
  val code: String
}

case object Columns extends GoogleSheetDimension {
  val code = "COLUMNS"
}

case object Rows extends GoogleSheetDimension {
  val code = "ROWS"
}
