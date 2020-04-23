package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSpreadsheet}
import com.google.api.services.sheets.v4.model.{BatchUpdateValuesResponse, RowData, UpdateValuesResponse}
import retry.{RetryDetails, RetryPolicy}

class GoogleSpreadsheetSync[F[_]: Sync](
    spreadsheet: GoogleSpreadsheet,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(
    implicit timer: Timer[F]
) extends Retry[F](retryPolicy, onError) {

  def id: String = spreadsheet.id

  def title: String = spreadsheet.title

  def maybeReadSpreadsheet[T](ranges: List[String], parseFields: List[List[String]] => List[T]): F[List[T]] =
    retry(
      spreadsheet.maybeReadSpreadsheet(ranges, parseFields)
    )

  def readRows(range: String): F[Seq[RowData]] =
    retry(
      spreadsheet.readRows(range)
    )

  def readRows(ranges: List[String]): F[Seq[Seq[RowData]]] =
    retry(
      spreadsheet.readRows(ranges)
    )

  def writeRanges(sheets: List[(String, Seq[Seq[AnyRef]])]): F[Unit] =
    retry(
      spreadsheet.writeRanges(sheets)
    )

  def writeRange(range: String, content: Seq[Seq[AnyRef]]): F[Unit] =
    retry(
      spreadsheet.writeRange(range, content)
    )

  def retrieveSheetsIds(): F[Map[String, Int]] =
    retry(
      spreadsheet.retrieveSheetsIds()
    )
}

object GoogleSpreadsheetSync {

  def createWithSheets[F[_]: Sync](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String],
      retryPolicy: RetryPolicy[F],
      onError: (Throwable, RetryDetails) => F[Unit]
  )(implicit timer: Timer[F]): F[GoogleSpreadsheetSync[F]] = {
    val spreadsheetRetried = Retry.retry[F, GoogleSpreadsheet](
      policy = retryPolicy,
      onError = onError
    )(
      GoogleSpreadsheet.createWithSheets(authenticator, spreadSheetTitle, titles)
    )

    spreadsheetRetried.map { spreadsheet =>
      new GoogleSpreadsheetSync[F](
        spreadsheet = spreadsheet,
        retryPolicy = retryPolicy,
        onError = onError
      )
    }
  }

  def createWithSheets[F[_]: Sync](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String]
  )(implicit timer: Timer[F]): F[GoogleSpreadsheetSync[F]] =
    createWithSheets(
      authenticator,
      spreadSheetTitle,
      titles,
      Retry.defaultPolicy[F],
      Retry.defaultOnError[F]
    )
}
