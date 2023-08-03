package com.colisweb.gdrive.client.sheets

import com.colisweb.gdrive.client.sheets.formatting.{GoogleSheetCellFormat, GoogleSheetField}
import com.google.api.services.sheets.v4.model._

import scala.jdk.CollectionConverters._
import scala.util.Try

trait GoogleBatchRequest {
  def request: Request
}

final case class AutoResize(id: String, sheetId: Int, dimension: GoogleSheetDimension) extends GoogleBatchRequest {
  def request: Request = {
    val dimensionsRange = new DimensionRange().setSheetId(sheetId).setDimension(dimension.code)
    val resizeRequest   = new AutoResizeDimensionsRequest().setDimensions(dimensionsRange)

    new Request().setAutoResizeDimensions(resizeRequest)
  }
}

final case class FormatRange(
    range: GoogleGridRange,
    cellFormat: GoogleSheetCellFormat,
    fields: GoogleSheetField
) extends GoogleBatchRequest {
  def request: Request = {
    val cellData          = new CellData().setUserEnteredFormat(cellFormat.format)
    val repeatCellRequest = new RepeatCellRequest().setRange(range.toGoogle).setCell(cellData).setFields(fields.field)

    new Request().setRepeatCell(repeatCellRequest)
  }
}

final case class CopyPaste(source: GoogleGridRange, destination: GoogleGridRange) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCopyPaste(new CopyPasteRequest().setSource(source.toGoogle).setDestination(destination.toGoogle))
}

final case class CutPaste(source: GoogleGridRange, destination: GoogleGridCoordinate) extends GoogleBatchRequest {
  def request: Request =
    new Request().setCutPaste(new CutPasteRequest().setSource(source.toGoogle).setDestination(destination.toGoogle))
}

final case class AppendDimension(
    sheetId: Int,
    dimension: GoogleSheetDimension,
    length: Int
) extends GoogleBatchRequest {
  def request: Request =
    new Request().setAppendDimension(
      new AppendDimensionRequest().setSheetId(sheetId).setDimension(dimension.code).setLength(length)
    )
}

final case class InsertDimension(
    sheetId: Int,
    dimension: GoogleSheetDimension,
    startIndex: Int,
    endIndex: Int
) extends GoogleBatchRequest {
  def request: Request = {
    val dimensionRange = new DimensionRange()
      .setSheetId(sheetId)
      .setStartIndex(startIndex)
      .setEndIndex(endIndex)
      .setDimension(dimension.code)

    new Request().setInsertDimension(new InsertDimensionRequest().setRange(dimensionRange))
  }
}

final case class AddBigQueryDataSource(
    bigQueryProjectId: String,
    bigQueryTableId: String,
    bigQueryDatasetId: String,
    sheetId: Int
) extends GoogleBatchRequest {
  def request: Request = {
    val tableSpec = new BigQueryTableSpec().setTableId(bigQueryTableId).setDatasetId(bigQueryDatasetId)
    val bigQuerySpec: DataSourceSpec = new DataSourceSpec().setBigQuery(
      new BigQueryDataSourceSpec().setProjectId(bigQueryProjectId).setTableSpec(tableSpec)
    )
    val dataSource = new DataSource().setSpec(bigQuerySpec)

    new Request().setAddDataSource(new AddDataSourceRequest().setDataSource(dataSource))
  }
}

object AddBigQueryDataSource {
  def extractDataSourceIdFromResponse(batchResponse: BatchUpdateSpreadsheetResponse): Option[String] = {
    Try {
      batchResponse.getReplies.asScala.toList.collectFirst {
        case response if response.getAddDataSource != null =>
          Option(response.getAddDataSource.getDataSource.getDataSourceId)
      }.flatten
    }.toOption.flatten
  }
}

final case class CreatePivotTableFromDataSource(
    spreadsheetId: String,
    dataSourceId: String,
    pivotTable: PivotTable,
    gridCoordinate: GoogleGridCoordinate
) extends GoogleBatchRequest {
  def request: Request = {
    val cellData = new CellData().setPivotTable(pivotTable.setDataSourceId(dataSourceId))
    val rowData  = new RowData().setValues(List(cellData).asJava)
    val updateCells =
      new UpdateCellsRequest().setRows(List(rowData).asJava).setFields("pivotTable").setStart(gridCoordinate.toGoogle)

    new Request().setUpdateCells(updateCells)
  }
}
