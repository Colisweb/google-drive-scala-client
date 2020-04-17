package com.colisweb.gdrive.client

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class GoogleSheetsClientTest extends AnyFlatSpec with Matchers {

  it should "write to and read from a google spreadsheet" in {
    val client      = GoogleClient("google-credentials.json", "RoutingAnalysis")
    val sheetNames  = List("foo", "toto")
    val spreadSheet = client.sheetsClient.createWithSheets("spreadsheet_name", sheetNames)
    val range       = s"${sheetNames(1)}!A1:C2"

    val data = Seq.tabulate(2, 3)((r, c) => s"data $r $c")

    spreadSheet.writeRange(range, data)

    val rowData = spreadSheet
      .readRows(range)
      .map(_.getValues.asScala)
      .map(_.map(_.getFormattedValue))

    rowData shouldBe data

    client.driveClient.delete(spreadSheet.id)
  }
}
