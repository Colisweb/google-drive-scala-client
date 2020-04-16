package com.colisweb.gdrive.client

import cats.effect.IO
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.{GridData, RowData, UpdateValuesResponse, ValueRange}

import scala.collection.JavaConverters._

final case class GoogleSheetsClient(sheetsService: Sheets) {
  def maybeReadSpreadsheet[T](
      idOrError: Either[GoogleError, String],
      ranges: List[String],
      parseFields: List[List[String]] => List[T]
  ): IO[List[T]] = {
    def readSpreadsheet(id: String): IO[List[T]] = {
      IO {
        sheetsService
          .spreadsheets()
          .get(id)
          .setRanges(ranges.asJava)
          .setIncludeGridData(true)
          .execute()
      }.map { data =>
        val sheet     = data.getSheets.get(0)
        val sheetData = sheet.getData.asScala.toList

        // Removing headers' row
        parseFields(readGridDataAsStringAndTransposeToColumnFirst(sheetData).tail)
      }
    }

    idOrError match {
      case Left(_)   => IO(Nil)
      case Right(id) => readSpreadsheet(id)
    }
  }

  def readRows(range: String, sheetId: String): Seq[RowData] =
    sheetsService
      .spreadsheets()
      .get(sheetId)
      .setRanges(List(range).asJava)
      .setIncludeGridData(true)
      .execute()
      .getSheets
      .get(0)
      .getData
      .get(0)
      .getRowData
      .asScala

  def writeRange(sheetId: String, range: String, content: Seq[Seq[AnyRef]]): UpdateValuesResponse = {
    val values = new ValueRange
    values.setValues(content.map(_.asJava).asJava)
    sheetsService
      .spreadsheets()
      .values()
      .update(sheetId, range, values)
      .setValueInputOption("RAW")
      .execute()
  }

  def retrieveSheetsIds(spreadsheetId: String): IO[Map[String, Int]] =
    IO {
      sheetsService
        .spreadsheets()
        .get(spreadsheetId)
        .execute()
    }.map { spreadsheet =>
      spreadsheet.getSheets.asScala.map { sheet =>
        val properties = sheet.getProperties
        properties.getTitle -> properties.getSheetId.toInt
      }.toMap
    }

  private def readGridDataAsStringAndTransposeToColumnFirst(sheetData: List[GridData]): List[List[String]] = {
    def rowIsNotEmpty: RowData => Boolean = _.getValues.asScala.forall(_.getEffectiveValue != null)

    sheetData.map { rangeData =>
      val rows                  = rangeData.getRowData.asScala.toList
      val rowsWithoutEmptyCells = rows.filter(rowIsNotEmpty)

      rowsWithoutEmptyCells.flatMap(_.getValues.asScala.toList.map(_.getFormattedValue))
    }.transpose
  }
}
