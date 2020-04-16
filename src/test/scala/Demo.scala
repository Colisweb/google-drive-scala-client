import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
  val rows = GoogleSheetsClient(client.sheetsService)
    .readRows("A1:C4", sheetId)
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

  GoogleSheetsClient(client.sheetsService).writeRange(sheetId, "A1:D3", rows.transpose)

}
