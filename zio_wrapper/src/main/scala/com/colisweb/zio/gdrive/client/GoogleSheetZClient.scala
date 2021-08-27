package com.colisweb.zio.gdrive.client

import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.sheets._
import com.google.api.services.sheets.v4.model.RowData
import zio.clock.Clock
import zio.{RIO, Schedule}

class GoogleSheetZClient(
    authenticator: GoogleAuthenticator,
    retryPolicy: Schedule[Any, Throwable, Any] = RetryPolicies.default
) {

  private val retry  = new Retry(retryPolicy)
  private val client = new GoogleSheetClient(authenticator)

  def createSpreadsheet(name: String, sheetsProperties: List[GoogleSheetProperties]): RIO[Clock, String] =
    retry(
      client.createSpreadsheet(name, sheetsProperties)
    )

  def maybeReadSpreadsheet[T](
      id: String,
      ranges: List[String],
      parseFields: List[List[String]] => List[T]
  ): RIO[Clock, List[T]] =
    retry(
      client.maybeReadSpreadsheet(id, ranges, parseFields)
    )

  def readRows(id: String, range: String): RIO[Clock, Seq[RowData]] =
    retry(
      client.readRows(id, range)
    )

  def readRows(id: String, ranges: List[String]): RIO[Clock, Seq[Seq[RowData]]] =
    retry(
      client.readRows(id, ranges)
    )

  def writeRanges(
      id: String,
      sheets: List[SheetRangeContent],
      inputOption: InputOption = InputOptionRaw
  ): RIO[Clock, Unit] =
    retry(
      client.writeRanges(id, sheets, inputOption)
    ).unit

  def writeRange(id: String, sheet: SheetRangeContent, inputOption: InputOption = InputOptionRaw): RIO[Clock, Unit] =
    retry(
      client.writeRange(id, sheet, inputOption)
    ).unit

  def retrieveSheetsIds(id: String): RIO[Clock, Map[String, Int]] =
    retry(
      client.retrieveSheetsIds(id)
    )

  def batchRequests(spreadsheetId: String, requests: List[GoogleBatchRequest]): RIO[Clock, Unit] =
    retry(
      client.batchRequests(spreadsheetId, requests)
    ).unit
}
