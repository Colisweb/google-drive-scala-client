import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
  val sheets = client.sheetsClient
  val drive  = client.driveClient

  val spreadSheet = sheets.createWithSheets("spreadsheet_name", List("foo"))
  println(spreadSheet.id)

  val rows = spreadSheet
    .readRows(List("A1:C2", "A2:D3"))
    .flatten
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

  spreadSheet.writeRange("G1:H4", rows.transpose)

  drive.share(spreadSheet.id, "michel.daviot@colisweb.com", GoogleDriveRole.commenter)
}
