package org.quickdev.sdk.plugin.lowcoderapi;

import org.quickdev.sdk.models.DatasourceConnectionConfig;

public class LowcoderApiDatasourceConfig implements DatasourceConnectionConfig {

    public static final LowcoderApiDatasourceConfig INSTANCE = new LowcoderApiDatasourceConfig();

    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        return detailConfig;
    }
}
