package com.colisweb.gdrive.client

sealed trait InputOption {
  def value: String
}

case object InputOptionRaw extends InputOption {
  override def value = "RAW"
}

case object InputOptionUserEntered extends InputOption {
  override def value = "USER_ENTERED"
}
