package com.colisweb.gdrive.client.formatting

sealed trait HorizontalAlignment {
  val code: String
}

case object Left extends HorizontalAlignment {
  val code = "LEFT"
}

case object Center extends HorizontalAlignment {
  val code = "CENTER"
}

case object Right extends HorizontalAlignment {
  val code = "RIGHT"
}
