import com.colisweb.gdrive.client._

import scala.jdk.CollectionConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
  val drive         = new GoogleDriveClient(authenticator)
  val sheets        = new GoogleSheetClient(authenticator)

  val spreadsheetId = sheets.createSpreadsheet("spreadsheet_name", List("foo"))

  println(spreadsheetId)

  val rows = sheets
    .readRows(spreadsheetId, List("A1:C2", "A2:D3"))
    .flatten
    .map(_.getValues.asScala.toList)
    .map(_.map(_.getFormattedValue))
    .toList

  rows.foreach(println)

  sheets.writeRange(spreadsheetId, SheetRangeContent("G1:H4", rows.transpose))

  drive.share(spreadsheetId, "michel.daviot@colisweb.com", GoogleDriveRole.commenter)
}
