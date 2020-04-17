package com.colisweb.gdrive.client

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.{
  BatchUpdateSpreadsheetRequest,
  DeleteSheetRequest,
  GridData,
  Request,
  RowData,
  Spreadsheet,
  UpdateValuesResponse,
  ValueRange
}

import scala.collection.JavaConverters._

final case class GoogleSheetsClient(sheetsService: Sheets, sheetId: String) {
  def maybeReadSpreadsheet[T](
      ranges: List[String],
      parseFields: List[List[String]] => List[T]
  ): List[T] = {
    val sheetData = sheetsService
      .spreadsheets()
      .get(sheetId)
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

  def readRows(range: String): Seq[RowData] = readRows(List(range))

  def createSheet(): String =
    sheetsService.spreadsheets().create(new Spreadsheet).execute().getSpreadsheetId

  def readRows(ranges: List[String]): Seq[RowData] =
    sheetsService
      .spreadsheets()
      .get(sheetId)
      .setRanges(ranges.asJava)
      .setIncludeGridData(true)
      .execute()
      .getSheets
      .get(0)
      .getData
      .get(0)
      .getRowData
      .asScala

  def writeRange(range: String, content: Seq[Seq[AnyRef]]): UpdateValuesResponse = {
    val values = new ValueRange
    values.setValues(content.map(_.asJava).asJava)
    sheetsService
      .spreadsheets()
      .values()
      .update(sheetId, range, values)
      .setValueInputOption("RAW")
      .execute()
  }

  def retrieveSheetsIds(): Map[String, Int] =
    sheetsService
      .spreadsheets()
      .get(sheetId)
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
