package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import retry.{RetryDetails, RetryPolicies, RetryPolicy, retryingOnAllErrors}

import scala.concurrent.duration.DurationLong

class Retry[F[_]](policy: RetryPolicy[F], onError: (Throwable, RetryDetails) => F[Unit])(implicit
    F: Sync[F],
    timer: Timer[F]
) {

  def retry[A](action: => A): F[A] = Retry.retry(policy, onError)(action)
}

object Retry {

  def retry[F[_], A](
      policy: RetryPolicy[F],
      onError: (Throwable, RetryDetails) => F[Unit]
  )(action: => A)(implicit F: Sync[F], time: Timer[F]): F[A] =
    retryingOnAllErrors(
      policy = policy,
      onError = onError
    )(
      action = F.delay(action)
    )

  def defaultOnError[F[_]](error: Throwable, retryDetails: RetryDetails)(implicit F: Sync[F]): F[Unit] = {
    val message =
      s"""
         |Health check failed with the following error: ${error.getMessage}
         |${retryDetails.retriesSoFar} retries so far
         |Retrying in ${retryDetails.upcomingDelay.get}
         |""".stripMargin

    F.delay(System.err.println(message))
  }

  def defaultPolicy[F[_]: Sync]: RetryPolicy[F] = exponentialAttempts() join maxAttempts()

  def exponentialAttempts[F[_]: Sync](baseDelaySec: Long = 30): RetryPolicy[F] = RetryPolicies.exponentialBackoff[F](
    baseDelaySec.seconds
  )

  def maxAttempts[F[_]: Sync](maxRetry: Int = 5): RetryPolicy[F] = RetryPolicies.limitRetries[F](maxRetry)

}
