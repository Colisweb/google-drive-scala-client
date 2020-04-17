package com.colisweb.gdrive.client

import org.scalatest.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import scala.collection.JavaConverters._

class GoogleSheetsClientTest extends AnyFlatSpec with Matchers {

  it should "write to and read from a google sheet" in {
    val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
    val sheet  = client.sheetsClient.createSheet()

    val data = Seq.tabulate(2, 3)((r, c) => s"data $r $c")
    sheet.writeRange("A1:C2", data)

    val rowData = sheet
      .readRows("A1:C2")
      .map(_.getValues.asScala)
      .map(_.map(_.getFormattedValue))

    rowData shouldBe data

    client.driveClient.delete(sheet.sheetId)
  }
}
