import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1R_rshsVSQkfehwP-4R_Fb2f5Ilgwjeu-nzxvStSPRg8"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis").sheetsClient(sheetId)
  val rows = client
    .readRows("A1:C3")
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

  client.writeRange("A1:C3", rows.transpose)

}
