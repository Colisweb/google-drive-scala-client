package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{
  GridProperties => GoogleGridProperties,
  SheetProperties => GoogleSheetProperties
}

case class SheetProperties(title: String, gridProperties: GridProperties = GridProperties()) {

  val toGoogle: GoogleSheetProperties =
    new GoogleSheetProperties().setTitle(title).setGridProperties(gridProperties.toGoogle)

}

case class GridProperties(frozenRowCount: Int = 0, frozenColumnCount: Int = 0) {

  val toGoogle: GoogleGridProperties =
    new GoogleGridProperties().setFrozenRowCount(frozenRowCount).setFrozenColumnCount(frozenColumnCount)

}
