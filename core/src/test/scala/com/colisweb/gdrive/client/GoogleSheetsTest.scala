package com.colisweb.gdrive.client

import com.colisweb.gdrive.client.GoogleSheetsTest.DataSourceConfig
import com.colisweb.gdrive.client.drive.GoogleDriveClient
import com.colisweb.gdrive.client.sheets.AddBigQueryDataSource.extractDataSourceIdFromResponse
import com.colisweb.gdrive.client.sheets.GooglePivotTable.GooglePivotGroup
import com.colisweb.gdrive.client.sheets._
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser.decode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.jdk.CollectionConverters._

class GoogleSheetsTest extends AnyFlatSpec with Matchers {

  it should "test write to and read from a google spreadsheet" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetProperties =
      List(GoogleSheetProperties("foo"), GoogleSheetProperties("toto", GoogleGridProperties(frozenRowCount = 1)))
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetProperties)

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

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetProperties =
      List(GoogleSheetProperties("foo"), GoogleSheetProperties("toto", GoogleGridProperties(frozenRowCount = 1)))
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetProperties)

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
    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetProperties =
      List(GoogleSheetProperties("foo"), GoogleSheetProperties("toto", GoogleGridProperties(frozenRowCount = 1)))
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetProperties)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    a[GoogleJsonResponseException] should be thrownBy sheets.writeRange(
      spreadsheetId,
      SheetRangeContent("invalid_sheet_name!A1:C2", data)
    )

    drive.delete(spreadsheetId)
  }

  it should "test writing with an inverted range" in {
    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)
    val sheets        = new GoogleSheetClient(authenticator)

    val sheetProperties =
      List(GoogleSheetProperties("foo"), GoogleSheetProperties("toto", GoogleGridProperties(frozenRowCount = 1)))
    val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", sheetProperties)

    val data = List.tabulate(2, 3)((r, c) => s"data $r $c")

    a[GoogleJsonResponseException] should be thrownBy sheets.writeRange(
      spreadsheetId,
      SheetRangeContent("toto!B1:A1", data)
    )

    drive.delete(spreadsheetId)
  }

  it should "create a pivot table with a big query data source" in {
    val authenticator        = GoogleAuthenticator.fromResource("google-credentials.json", Some("Simulation"))
    val dataSourceConfigFile = Source.fromResource("big-query-data-source-config.json").getLines().mkString("\n")
    val dataSourceConfig     = decode[DataSourceConfig](dataSourceConfigFile).toOption.get
    val drive                = new GoogleDriveClient(authenticator)
    val sheets               = new GoogleSheetClient(authenticator)

    val sheetProperties = List(GoogleSheetProperties("foo"))
    val spreadsheetId   = sheets.createSpreadsheet("spreadsheet_name", sheetProperties)

    val sheetId = sheets.retrieveSheetsProperties(spreadsheetId).head.getSheetId

    val addBigQueryDataSourceResponse =
      sheets.batchRequests(
        spreadsheetId,
        List(
          AddBigQueryDataSource(
            dataSourceConfig.bigQueryProjectId,
            dataSourceConfig.bigQueryTableId,
            dataSourceConfig.bigQueryDatasetId
          )
        )
      )

    val dataSourceId = extractDataSourceIdFromResponse(addBigQueryDataSourceResponse)
    dataSourceId.isDefined shouldBe true

    val pivotTable = GooglePivotTable(
      rows = List(GooglePivotGroup("name", "ASCENDING"), GooglePivotGroup("date", "ASCENDING")),
      values = Nil
    )

    sheets.batchRequests(
      spreadsheetId,
      List(CreatePivotTableFromDataSource(spreadsheetId, pivotTable, GoogleGridCoordinate(sheetId, 1, 1)))
    )

    drive.delete(spreadsheetId)
  }
}

object GoogleSheetsTest {

  final case class DataSourceConfig(
      bigQueryProjectId: String,
      bigQueryTableId: String,
      bigQueryDatasetId: String
  )

  implicit val config: Configuration                          = Configuration.default
  implicit val dataSourceConfigCodec: Codec[DataSourceConfig] = deriveConfiguredCodec
}
