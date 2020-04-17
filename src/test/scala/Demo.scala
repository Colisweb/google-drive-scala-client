import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
  val sheets = client.sheetsClient(sheetId)
  val drive  = client.driveClient

  val id = sheets.createSheet()
  println(id)

  val rows = sheets
    .readRows(List("A1:C2", "A2:D3"))
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

  client.sheetsClient(id).writeRange("G1:H4", rows.transpose)

  drive.share(id, "michel.daviot@colisweb.com", GoogleDriveRole.commenter)
}
