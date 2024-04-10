package org.quickdev.impl.mock;

import org.quickdev.domain.datasource.model.Datasource;
import org.quickdev.sdk.models.DatasourceConnectionConfig;

public record MockDatasourceConnectionConfig(Datasource datasource) implements DatasourceConnectionConfig {

    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        throw new UnsupportedOperationException();
    }
}
