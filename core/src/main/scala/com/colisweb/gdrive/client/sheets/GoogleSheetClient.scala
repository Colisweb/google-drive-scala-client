package com.colisweb.gdrive.client.sheets

import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.GoogleUtilities._
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._
import com.google.auth.http.HttpCredentialsAdapter

import scala.jdk.CollectionConverters._

class GoogleSheetClient(authenticator: GoogleAuthenticator) {

  val service: Sheets =
    new Sheets.Builder(
      authenticator.httpTransport,
      authenticator.jsonFactory,
      new HttpCredentialsAdapter(authenticator.credentials)
    ).setApplicationName(authenticator.applicationName)
      .build()

  def createSpreadsheet(name: String, sheetProperties: List[GoogleSheetProperties]): String = {

    val spreadsheetProperties = new SpreadsheetProperties().setTitle(name)
    val sheets                = sheetProperties.map(properties => (new Sheet).setProperties(properties.toGoogle))
    val spreadSheet           = (new Spreadsheet).setProperties(spreadsheetProperties).setSheets(sheets.asJava)

    service
      .spreadsheets()
      .create(spreadSheet)
      .execute()
      .getSpreadsheetId
  }

  def maybeReadSpreadsheet[T](
      id: String,
      ranges: List[String],
      parseFields: List[List[String]] => List[T]
  ): List[T] = {
    val sheetData = service
      .spreadsheets()
      .get(id)
      .setRanges(ranges.asJava)
      .setIncludeGridData(true)
      .execute()
      .getSheets
      .get(0)
      .getData
      .asScalaListNotNull

    // Removing headers' row
    parseFields(readGridDataAsStringAndTransposeToColumnFirst(sheetData).tail)
  }

  def readRows(id: String, range: String): Seq[RowData] = readRows(id, List(range)).flatten

  def readRows(id: String, ranges: List[String]): Seq[Seq[RowData]] = {
    service
      .spreadsheets()
      .get(id)
      .setRanges(ranges.asJava)
      .setIncludeGridData(true)
      .execute()
      .getSheets
      .get(0)
      .getData
      .asScalaListNotNull
      .map(_.getRowData.asScalaListNotNull)
  }

  def writeRanges(
      id: String,
      sheets: List[SheetRangeContent],
      inputOption: InputOption = InputOptionRaw
  ): BatchUpdateValuesResponse = {

    val data = sheets.map(_.toValueRange)
    val body = new BatchUpdateValuesRequest()
      .setValueInputOption(inputOption.value)
      .setData(data.asJava)

    service
      .spreadsheets()
      .values()
      .batchUpdate(id, body)
      .execute()
  }

  def writeRange(
      id: String,
      sheet: SheetRangeContent,
      inputOption: InputOption = InputOptionRaw
  ): UpdateValuesResponse = {
    service
      .spreadsheets()
      .values()
      .update(id, sheet.range, sheet.toValueRange)
      .setValueInputOption(inputOption.value)
      .execute()
  }

  def retrieveSheetsIds(id: String): Map[String, Int] =
    service
      .spreadsheets()
      .get(id)
      .execute
      .getSheets
      .asScalaListNotNull
      .map { sheet =>
        val properties = sheet.getProperties
        properties.getTitle -> properties.getSheetId.toInt
      }
      .toMap

  def retrieveSheetsProperties(id: String): List[SheetProperties] =
    service
      .spreadsheets()
      .get(id)
      .execute
      .getSheets
      .asScalaListNotNull
      .map(_.getProperties)

  private def readGridDataAsStringAndTransposeToColumnFirst(sheetData: List[GridData]): List[List[String]] = {
    def rowIsNotEmpty: RowData => Boolean = _.getValues.asScalaListNotNull.forall(_.getEffectiveValue != null)

    sheetData.map { rangeData =>
      val rows                  = rangeData.getRowData.asScalaListNotNull
      val rowsWithoutEmptyCells = rows.filter(rowIsNotEmpty)

      rowsWithoutEmptyCells.flatMap(_.getValues.asScalaListNotNull.map(_.getFormattedValue))
    }.transpose
  }

  def batchRequests(spreadsheetId: String, requests: List[GoogleBatchRequest]): BatchUpdateSpreadsheetResponse =
    service
      .spreadsheets()
      .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests.map(_.request).asJava))
      .execute()
}
