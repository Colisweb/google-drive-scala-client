import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
  val sheets = client.sheetsClient
  val drive  = client.driveClient

  val sheet = sheets.createSheet()
  println(sheet.sheetId)

  val rows = sheet
    .readRows(List("A1:C2", "A2:D3"))
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

  sheet.writeRange("G1:H4", rows.transpose)

  drive.share(sheet.sheetId, "michel.daviot@colisweb.com", GoogleDriveRole.commenter)
}
