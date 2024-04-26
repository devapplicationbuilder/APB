package org.quickdev.plugin.mssql.model;

import static org.quickdev.sdk.exception.PluginCommonError.INVALID_QUERY_SETTINGS;
import static org.quickdev.sdk.util.JsonUtils.fromJson;
import static org.quickdev.sdk.util.JsonUtils.toJson;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.quickdev.sdk.exception.PluginException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class MssqlQueryConfig {
    private final String sql;
    private final boolean disablePreparedStatement;
    private final String mode;
    private final String guiStatementType;
    private final Map<String, Object> guiStatementDetail;

    @JsonCreator
    private MssqlQueryConfig(String sql, boolean disablePreparedStatement,
            String mode,
            @JsonProperty("commandType") String guiStatementType,
            @JsonProperty("command") Map<String, Object> guiStatementDetail) {
        this.sql = sql;
        this.disablePreparedStatement = disablePreparedStatement;
        this.mode = mode;
        this.guiStatementType = guiStatementType;
        this.guiStatementDetail = guiStatementDetail;
    }

    public static MssqlQueryConfig from(Map<String, Object> queryConfigs) {
        if (MapUtils.isEmpty(queryConfigs)) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "SQLSERVER_QUERY_CONFIG_EMPTY");
        }

        MssqlQueryConfig result = fromJson(toJson(queryConfigs), MssqlQueryConfig.class);
        if (result == null) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "INVALID_SQLSERVER_CONFIG_0");
        }
        return result;

    }

    public String getSql() {
        return sql.trim();
    }

    public boolean isGuiMode() {
        return "GUI".equalsIgnoreCase(mode);
    }

}
