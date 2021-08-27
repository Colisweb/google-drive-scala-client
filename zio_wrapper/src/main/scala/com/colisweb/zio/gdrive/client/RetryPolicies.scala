package com.colisweb.zio.gdrive.client

import zio.Schedule
import zio.duration.Duration

import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object RetryPolicies {

  val onlyTimeoutErrors: Schedule[Any, Throwable, Throwable] =
    Schedule.recurWhile[Throwable](_.isInstanceOf[SocketTimeoutException])

  val fiveExponentialAttempts: Schedule[Any, Any, (Duration, Long)] =
    Schedule.exponential(Duration(1, TimeUnit.MINUTES)) && Schedule.recurs(5)

  val default: Schedule[Any, Throwable, ((Duration, Long), Throwable)] =
    fiveExponentialAttempts && onlyTimeoutErrors
}
