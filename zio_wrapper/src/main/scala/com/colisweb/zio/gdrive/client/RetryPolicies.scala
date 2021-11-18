package com.colisweb.zio.gdrive.client

import zio.Schedule
import zio.duration.Duration

import java.util.concurrent.TimeUnit

object RetryPolicies {

  def exponentialAttempts(baseDelaySec: Long = 30): Schedule[Any, Any, (Duration, Long)] =
    Schedule.exponential(Duration(baseDelaySec, TimeUnit.SECONDS)) && Schedule.recurs(5)

  def maxAttempts(maxRetry: Int = 5): Schedule[Any, Any, Long] = Schedule.recurs(maxRetry)

  val default: Schedule[Any, Any, ((Duration, Long), Long)] = exponentialAttempts() && maxAttempts()
}
