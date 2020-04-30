package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.model.ValueRange
import scala.collection.JavaConverters._

case class SheetRangeContent(range: String, body: List[List[AnyRef]]) {

  private[client] def toValueRange: ValueRange = {
    new ValueRange()
      .setRange(range)
      .setValues(body.map(_.asJava).asJava)
  }
}
