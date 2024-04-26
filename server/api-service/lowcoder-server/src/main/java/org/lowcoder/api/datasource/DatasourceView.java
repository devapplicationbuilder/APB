package org.quickdev.api.datasource;

import org.quickdev.domain.datasource.model.Datasource;

public record DatasourceView(Datasource datasource, boolean edit, String creatorName) {

    public DatasourceView(Datasource datasource, boolean edit) {
        this(datasource, edit, null);
    }
}
