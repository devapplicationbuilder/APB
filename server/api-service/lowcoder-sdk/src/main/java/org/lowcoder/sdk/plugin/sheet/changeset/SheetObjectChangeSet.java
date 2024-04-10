package org.quickdev.sdk.plugin.sheet.changeset;


import static org.quickdev.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static org.quickdev.sdk.plugin.sheet.changeset.SheetChangeSetRow.fromJsonNode;

import java.util.Map;

import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.util.MustacheHelper;

import com.fasterxml.jackson.databind.JsonNode;

public class SheetObjectChangeSet extends SheetChangeSet {
    private final String str;

    public SheetObjectChangeSet(String str) {
        this.str = str;
    }

    @Override
    public SheetChangeSetRow render(Map<String, Object> requestMap) {
        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }
        return fromJsonNode(jsonNode);
    }

}
