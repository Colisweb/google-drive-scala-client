package com.colisweb.zio.gdrive.client

import zio.{RIO, Schedule, ZIO}
import zio.clock.Clock

class Retry(retryPolicy: Schedule[Any, Throwable, Any]) {

  def apply[A](computation: => A): RIO[Clock, A] =
    ZIO.effect(computation).retry(retryPolicy)
}
