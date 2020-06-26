package com.colisweb.gdrive.client.sheets.chart

import com.colisweb.gdrive.client.sheets.{GoogleBatchRequest, GoogleGridRange}
import com.google.api.services.sheets.v4.model._

import scala.jdk.CollectionConverters._

final case class LineChart(
    name: String,
    sheetId: Option[Int],
    lineSmoothing: Boolean,
    bottomAxisTitle: String,
    leftAxisTitle: String,
    domainsGridRanges: List[GoogleGridRange], // 1 distinct domain per GridRange
    seriesGridRanges: List[GoogleGridRange]   // 1 distinct source per GridRange
) extends GoogleBatchRequest {

  def request: Request = {
    val series = seriesGridRanges.map { gridRange =>
      val chartData = new ChartData().setSourceRange(new ChartSourceRange().setSources(List(gridRange.toGoogle).asJava))
      val lineStyle = new LineStyle().setWidth(3).setType("SOLID")

      new BasicChartSeries().setTargetAxis("LEFT_AXIS").setSeries(chartData).setLineStyle(lineStyle)
    }

    val domainSources = domainsGridRanges.map(g => new ChartSourceRange().setSources(List(g.toGoogle).asJava))
    val domains       = domainSources.map(s => new BasicChartDomain().setDomain(new ChartData().setSourceRange(s)))

    val axises = List(
      new BasicChartAxis().setTitle(bottomAxisTitle).setPosition("BOTTOM_AXIS"),
      new BasicChartAxis().setTitle(leftAxisTitle).setPosition("LEFT_AXIS")
    )

    val spec = new BasicChartSpec()
      .setChartType("LINE")
      .setLegendPosition("BOTTOM_LEGEND")
      .setLineSmoothing(lineSmoothing)
      .setAxis(axises.asJava)
      .setDomains(domains.asJava)
      .setSeries(series.asJava)

    val position =
      sheetId.fold(new EmbeddedObjectPosition().setNewSheet(true))(id => new EmbeddedObjectPosition().setSheetId(id))

    val chart = new EmbeddedChart()
      .setSpec(new ChartSpec().setTitle(name).setBasicChart(spec))
      .setPosition(position)

    new Request().setAddChart(new AddChartRequest().setChart(chart))
  }
}
