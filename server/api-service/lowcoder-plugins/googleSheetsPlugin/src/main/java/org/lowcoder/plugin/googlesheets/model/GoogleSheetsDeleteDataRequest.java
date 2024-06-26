package org.quickdev.plugin.googlesheets.model;


import static org.quickdev.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static org.quickdev.sdk.util.MustacheHelper.renderMustacheString;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quickdev.sdk.exception.PluginException;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoogleSheetsDeleteDataRequest implements GoogleSheetsActionRequest {
    private String spreadsheetId;
    private String sheetName;
    private int rowIndex;
    private String rowIndexString;

    @Override
    public void renderParams(Map<String, Object> paramMap) {
        spreadsheetId = renderMustacheString(spreadsheetId, paramMap);
        sheetName = renderMustacheString(sheetName, paramMap);
        String rowIndexTemp = renderMustacheString(rowIndexString, paramMap);
        try {
            rowIndex = Integer.parseInt(rowIndexTemp);
        } catch (NumberFormatException e) {
            throw new PluginException(DATASOURCE_ARGUMENT_ERROR, "DATASOURCE_ARGUMENT_ERROR");
        }
    }

    @Override
    public boolean hasInvalidData() {
        return StringUtils.isAnyBlank(spreadsheetId, sheetName);
    }
}
