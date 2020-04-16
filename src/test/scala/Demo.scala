import com.colisweb.gdrive.client._

import scala.collection.JavaConverters._

object Demo extends App {
  val sheetId = "1a0zzSgpder5tzfvc6HZAF14g97fOenz00solfZI7vm4"

  val client = GoogleClient("google-credentials.json", "RoutingAnalysis")
  val rows = GoogleSheetsClient(client.sheetsService)
    .readRows("A1:AL5", sheetId)
    .map(_.getValues.asScala)
    .map(_.map(_.getFormattedValue))

  rows.foreach(println)

}
