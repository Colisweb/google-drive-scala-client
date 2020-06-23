package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{CellFormat, NumberFormat, TextFormat}

object GoogleSheetFormat {

  val percentageType    = "PERCENT"
  val percentagePattern = "#0.00%"

  val percentageCellFormat: CellFormat =
    new CellFormat().setNumberFormat(new NumberFormat().setType(percentageType).setPattern(percentagePattern))

  val boldCellFormat: CellFormat = new CellFormat().setTextFormat(new TextFormat().setBold(true))

}
