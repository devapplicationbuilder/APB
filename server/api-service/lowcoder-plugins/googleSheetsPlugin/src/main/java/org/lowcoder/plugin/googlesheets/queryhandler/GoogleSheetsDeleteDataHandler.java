package org.quickdev.plugin.googlesheets.queryhandler;


import java.util.ArrayList;
import java.util.List;

import org.quickdev.plugin.googlesheets.model.GoogleSheetsDeleteDataRequest;
import org.quickdev.plugin.googlesheets.model.GoogleSheetsQueryExecutionContext;
import org.quickdev.sdk.models.QueryExecutionResult;
import org.quickdev.sdk.plugin.common.QueryExecutionUtils;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;

import reactor.core.publisher.Mono;

public class GoogleSheetsDeleteDataHandler extends GoogleSheetsActionHandler {

    @Override
    public String getActionType() {
        return DELETE_DATA;
    }

    @Override
    public Mono<QueryExecutionResult> execute(Object o, GoogleSheetsQueryExecutionContext context) {
        GoogleSheetsDeleteDataRequest googleSheetsActionRequest = (GoogleSheetsDeleteDataRequest) context.getGoogleSheetsActionRequest();
        final int rowDeleteIndex = googleSheetsActionRequest.getRowIndex() + 1;
        Sheets sheetService = GoogleSheetsGetPreParameters.GetSheetsService(context);
        return Mono.fromCallable(() -> {
                    int sheetId = sheetService.spreadsheets().get(googleSheetsActionRequest.getSpreadsheetId()).execute()
                            .getSheets()
                            .stream()
                            .map(Sheet::getProperties)
                            .filter(sheetProperties -> sheetProperties.getTitle().equals(googleSheetsActionRequest.getSheetName()))
                            .map(SheetProperties::getSheetId)
                            .findFirst()
                            .orElse(0);
                    BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                    Request request = new Request();
                    DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
                    DimensionRange dimensionRange = new DimensionRange();
                    dimensionRange.setDimension("ROWS");
                    dimensionRange.setStartIndex(rowDeleteIndex - 1);
                    dimensionRange.setEndIndex(rowDeleteIndex);
                    dimensionRange.setSheetId(sheetId);
                    deleteDimensionRequest.setRange(dimensionRange);
                    request.setDeleteDimension(deleteDimensionRequest);
                    List<Request> requests = new ArrayList<>();
                    requests.add(request);
                    content.setRequests(requests);
                    BatchUpdateSpreadsheetResponse response =
                            sheetService.spreadsheets().batchUpdate(googleSheetsActionRequest.getSpreadsheetId(), content).execute();
                    return QueryExecutionResult.success(response.values());
                })
                .subscribeOn(QueryExecutionUtils.querySharedScheduler());
    }
}
