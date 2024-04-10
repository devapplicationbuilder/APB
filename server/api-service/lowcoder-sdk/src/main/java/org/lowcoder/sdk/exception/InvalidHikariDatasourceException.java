package org.quickdev.sdk.exception;

import static org.quickdev.sdk.exception.PluginCommonError.CONNECTION_ERROR;

public class InvalidHikariDatasourceException extends PluginException {

    public InvalidHikariDatasourceException() {
        super(CONNECTION_ERROR, "CONNECTION_ERROR", "hikari datasource closed.");
    }
}
