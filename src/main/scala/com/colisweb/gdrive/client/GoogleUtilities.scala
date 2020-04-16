package com.colisweb.gdrive.client

import cats.effect.IO
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object GoogleUtilities {

  private val log = LoggerFactory.getLogger(GoogleUtilities.getClass)

  def sheetsBatchRequests(
      sheetsService: Sheets
  )(spreadsheetId: String, requests: List[Request]): IO[Unit] =
    if (requests.nonEmpty) {
      log.debug(s"Sending ${requests.size} batch requests.")

      IO {
        sheetsService
          .spreadsheets()
          .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests.asJava))
          .execute()

        ()
      }
    } else {
      IO.unit
    }

  def asRowData(data: List[String], format: CellFormat): RowData =
    new RowData().setValues(
      data.map { d =>
        new CellData()
          .setUserEnteredValue(new ExtendedValue().setStringValue(d))
          .setEffectiveFormat(format)
      }.asJava
    )

  def singleRowGridRange(rowIndex: Int, sheetId: Int): GridRange =
    new GridRange().setSheetId(sheetId).setStartRowIndex(rowIndex).setEndRowIndex(rowIndex + 1)

  def singleColumnGridRange(columnIndex: Int, sheetId: Int): GridRange =
    columnGridRange(columnIndex, sheetId, 1)

  def columnGridRange(columnIndex: Int, sheetId: Int, length: Int): GridRange =
    new GridRange().setSheetId(sheetId).setStartColumnIndex(columnIndex).setEndColumnIndex(columnIndex + length)

  def copyPaste(source: GridRange, destination: GridRange): Request =
    new Request().setCopyPaste(new CopyPasteRequest().setSource(source).setDestination(destination))
}
