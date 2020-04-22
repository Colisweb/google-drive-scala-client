package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import retry.{RetryDetails, RetryPolicy, retryingOnAllErrors}

class Retry[F[_]](policy: RetryPolicy[F], onError: (Throwable, RetryDetails) => F[Unit])(
    implicit F: Sync[F],
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
}
