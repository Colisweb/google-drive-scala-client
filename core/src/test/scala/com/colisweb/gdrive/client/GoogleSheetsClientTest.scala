package com.colisweb.gdrive.client

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class GoogleSheetsClientTest extends AnyFlatSpec with Matchers {

  it should "test write to and read from a google spreadsheet" in {

    val authenticator = GoogleAuthenticator("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)

    val sheetNames  = List("foo", "toto")
    val spreadSheet = GoogleSpreadsheet.createWithSheets(authenticator, "spreadsheet_name", sheetNames)

    val range1 = "toto!A1:C1"
    val range2 = "toto!A2:C2"
    val ranges = List(range1, range2)

    val data = Seq.tabulate(2, 3)((r, c) => s"data $r $c")

    spreadSheet.writeRange("toto!A1:C2", data)

    val rowData = spreadSheet
      .readRows(ranges)
      .flatten
      .map(_.getValues.asScala.map(_.getFormattedValue))

    rowData shouldBe data

    drive.delete(spreadSheet.id)
  }

  it should "test write in an invalid sheet name" in {
    val authenticator = GoogleAuthenticator("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)

    val sheetNames  = List("foo", "toto")
    val spreadSheet = GoogleSpreadsheet.createWithSheets(authenticator, "spreadsheet_name", sheetNames)

    val data = Seq.tabulate(2, 3)((r, c) => s"data $r $c")

    a [GoogleJsonResponseException] should be thrownBy spreadSheet.writeRange("invalid_sheet_name!A1:C2", data)

    drive.delete(spreadSheet.id)
  }
}
