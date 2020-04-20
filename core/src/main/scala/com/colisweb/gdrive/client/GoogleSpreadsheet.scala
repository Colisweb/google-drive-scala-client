package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._

import scala.collection.JavaConverters._

class GoogleSpreadsheet private (service: Sheets, spreadsheetId: String, spreadsheetTitle: String) {

  def title: String = spreadsheetTitle

  def id: String = spreadsheetId

  def maybeReadSpreadsheet[T](
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

  def readRows(range: String): Seq[RowData] = readRows(List(range)).flatten

  def readRows(ranges: List[String]): Seq[Seq[RowData]] =
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

  def writeRange(range: String, content: Seq[Seq[AnyRef]]): UpdateValuesResponse = {
    val values = new ValueRange
    values.setValues(content.map(_.asJava).asJava)
    service
      .spreadsheets()
      .values()
      .update(id, range, values)
      .setValueInputOption("RAW")
      .execute()
  }

  def retrieveSheetsIds(): Map[String, Int] =
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

object GoogleSpreadsheet {

  def createWithSheets(
      authenticator: GoogleAuthenticator,
      spreadsheetTitle: String,
      sheetsTitles: List[String]
  ): GoogleSpreadsheet = {

    val service: Sheets =
      new Sheets.Builder(
        authenticator.httpTransport,
        authenticator.jsonFactory,
        authenticator.credentials
      ).setApplicationName(authenticator.applicationName)
        .build()

    val properties  = new SpreadsheetProperties().setTitle(spreadsheetTitle)
    val sheets      = sheetsTitles.map(title => (new Sheet).setProperties((new SheetProperties).setTitle(title)))
    val spreadSheet = (new Spreadsheet).setProperties(properties).setSheets(sheets.asJava)
    val id = service
      .spreadsheets()
      .create(spreadSheet)
      .execute()
      .getSpreadsheetId

    new GoogleSpreadsheet(service, id, spreadsheetTitle)
  }

}
