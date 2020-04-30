package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._

import scala.collection.JavaConverters._

class GoogleSheetClient(authenticator: GoogleAuthenticator) {

  val service: Sheets =
    new Sheets.Builder(
      authenticator.httpTransport,
      authenticator.jsonFactory,
      authenticator.credentials
    ).setApplicationName(authenticator.applicationName)
      .build()

  def createSpreadsheet(name: String, sheetsTitles: List[String]): String = {

    val properties  = new SpreadsheetProperties().setTitle(name)
    val sheets      = sheetsTitles.map(title => (new Sheet).setProperties((new SheetProperties).setTitle(title)))
    val spreadSheet = (new Spreadsheet).setProperties(properties).setSheets(sheets.asJava)

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
      .asScala
      .toList

    // Removing headers' row
    parseFields(readGridDataAsStringAndTransposeToColumnFirst(sheetData).tail)
  }

  def readRows(id: String, range: String): Seq[RowData] = readRows(id, List(range)).flatten

  def readRows(id: String, ranges: List[String]): Seq[Seq[RowData]] =
    service
      .spreadsheets()
      .get(id)
      .setRanges(ranges.asJava)
      .setIncludeGridData(true)
      .execute()
      .getSheets
      .get(0)
      .getData
      .asScala
      .map(_.getRowData.asScala)

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
      .asScala
      .map { sheet =>
        val properties = sheet.getProperties
        properties.getTitle -> properties.getSheetId.toInt
      }
      .toMap

  private def readGridDataAsStringAndTransposeToColumnFirst(sheetData: List[GridData]): List[List[String]] = {
    def rowIsNotEmpty: RowData => Boolean = _.getValues.asScala.forall(_.getEffectiveValue != null)

    sheetData.map { rangeData =>
      val rows                  = rangeData.getRowData.asScala.toList
      val rowsWithoutEmptyCells = rows.filter(rowIsNotEmpty)

      rowsWithoutEmptyCells.flatMap(_.getValues.asScala.toList.map(_.getFormattedValue))
    }.transpose
  }
}
