package com.colisweb.gdrive.client

sealed abstract case class InputOption(value: String)
final case class InputOptionRaw() extends InputOption("RAW")
final case class InputOptionUserEntered() extends InputOption("USER_ENTERED")

