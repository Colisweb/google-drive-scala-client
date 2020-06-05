package com.colisweb.gdrive.client

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class GoogleSheetsTest extends AnyFlatSpec with Matchers {

  it should "test write to and read from a google spreadsheet" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetNames    = List("foo", "toto")
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetNames)

    val range1 = "toto!A1:C1"
    val range2 = "toto!A2:C2"
    val ranges = List(range1, range2)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    sheets.writeRange(spreadsheetId, SheetRangeContent("toto!A1:C2", data))

    val rowData = sheets
      .readRows(spreadsheetId, ranges)
      .flatten
      .map(_.getValues.asScala.map(_.getFormattedValue))

    rowData shouldBe data

    drive.delete(spreadsheetId)
  }

  it should "test writing with a range containing only the starting cell" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetNames    = List("foo", "toto")
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetNames)

    val range1 = "toto!A1:C1"
    val range2 = "toto!A2:C2"
    val ranges = List(range1, range2)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    sheets.writeRange(spreadsheetId, SheetRangeContent("toto!A1", data))

    val rowData = sheets
      .readRows(spreadsheetId, ranges)
      .flatten
      .map(_.getValues.asScala.map(_.getFormattedValue))

    rowData shouldBe data

    drive.delete(spreadsheetId)
  }

  it should "test writing in an invalid sheet name" in {
    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetNames    = List("foo", "toto")
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetNames)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    a[GoogleJsonResponseException] should be thrownBy sheets.writeRange(
      spreadsheetId,
      SheetRangeContent("invalid_sheet_name!A1:C2", data)
    )

    drive.delete(spreadsheetId)
  }

  it should "test writing with an inverted range" in {
    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetNames    = List("foo", "toto")
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetNames)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    a[GoogleJsonResponseException] should be thrownBy sheets.writeRange(
      spreadsheetId,
      SheetRangeContent("toto!B1:A1", data)
    )

    drive.delete(spreadsheetId)
  }
}
