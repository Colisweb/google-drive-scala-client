package com.colisweb.gdrive.client.formatting

import com.google.api.services.sheets.v4.model.TextFormat

// TODO? it's a monoid!
trait GoogleSheetTextFormat {
  def combine: TextFormat => TextFormat

  def format: TextFormat = combine(new TextFormat)
}

object GoogleSheetTextFormat {

  def combineMultipleFormats(formats: List[GoogleSheetTextFormat]): GoogleSheetTextFormat =
    new GoogleSheetTextFormat {
      val combine: TextFormat => TextFormat =
        format => formats.foldLeft(format) { case (acc, next) => next.combine(acc) }
    }

}

final case class FontSize(size: Int) extends GoogleSheetTextFormat {
  val combine: TextFormat => TextFormat = _.setFontSize(size)
}

final case class FontFamily(font: String) extends GoogleSheetTextFormat {
  val combine: TextFormat => TextFormat = _.setFontFamily(font)
}

case object Bold extends GoogleSheetTextFormat {
  val combine: TextFormat => TextFormat = _.setBold(true)
}

case object Italic extends GoogleSheetTextFormat {
  val combine: TextFormat => TextFormat = _.setItalic(true)
}
