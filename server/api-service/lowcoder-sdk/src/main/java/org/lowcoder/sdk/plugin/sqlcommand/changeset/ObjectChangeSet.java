package org.quickdev.sdk.plugin.sqlcommand.changeset;

import static org.quickdev.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.Map;
import java.util.Set;

import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.util.MustacheHelper;

import com.fasterxml.jackson.databind.JsonNode;

public class ObjectChangeSet extends ChangeSet {
    private final String str;

    public ObjectChangeSet(String str) {
        this.str = str;
    }

    @Override
    public ChangeSetRow render(Map<String, Object> requestMap) {
        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }

        return new ChangeSetRow(jsonNode);
    }

    @Override
    public Set<String> extractMustacheKeys() {
        return MustacheHelper.extractMustacheKeysWithCurlyBraces(str);
    }
}
