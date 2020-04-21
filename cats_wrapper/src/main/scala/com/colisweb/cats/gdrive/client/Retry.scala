package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import retry.{RetryDetails, RetryPolicy, retryingOnAllErrors}

class Retry[F[_]](policy: RetryPolicy[F])(implicit F: Sync[F], timer: Timer[F]) {

  val retry = Retry.retry(policy)
}

object Retry {

  def retry[F[_], A](policy: RetryPolicy[F])(action: => A)(implicit F: Sync[F], time: Timer[F]): F[A] =
    retryingOnAllErrors(
      policy = policy,
      onError = onError[F]
    )(
      action = F.delay(action)
    )

  private def onError[F[_]](error: Throwable, retryDetails: RetryDetails)(implicit F: Sync[F]): F[Unit] = {
    val message =
      s"""
         |action failed with following error: ${error.getMessage}
         |${retryDetails.retriesSoFar} retries so far
         |Retrying in ${retryDetails.upcomingDelay.get}
         |""".stripMargin

    F.delay(System.err.println(message))
  }
}
