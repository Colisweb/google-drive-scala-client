package com.colisweb.gdrive.client

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class GoogleSheetsClientTest extends AnyFlatSpec with Matchers {

  it should "write to and read from a google spreadsheet" in {

    val authenticator = GoogleAuthenticator("google-credentials.json", "RoutingAnalysis")
    val sheets        = new GoogleSheetsClient(authenticator)
    val drive         = new GoogleDriveClient(authenticator)

    val sheetNames  = List("foo", "toto")
    val spreadSheet = sheets.createWithSheets("spreadsheet_name", sheetNames)
    val range1      = "toto!A1:C1"
    val range2      = "toto!A2:C2"
    val ranges      = List(range1, range2)

    val data = Seq.tabulate(2, 3)((r, c) => s"data $r $c")

    spreadSheet.writeRange("toto!A1:C2", data)

    val rowData = spreadSheet
      .readRows(ranges)
      .flatten
      .map(_.getValues.asScala.map(_.getFormattedValue))

    rowData shouldBe data

    drive.delete(spreadSheet.id)
  }
}
