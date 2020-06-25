package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.{GridProperties, SheetProperties}

case class GoogleSheetProperties(title: String, gridProperties: GoogleGridProperties = GoogleGridProperties()) {

  val toGoogle: SheetProperties =
    new SheetProperties().setTitle(title).setGridProperties(gridProperties.toGoogle)

}

case class GoogleGridProperties(frozenRowCount: Int = 0, frozenColumnCount: Int = 0) {

  val toGoogle: GridProperties =
    new GridProperties().setFrozenRowCount(frozenRowCount).setFrozenColumnCount(frozenColumnCount)

}
