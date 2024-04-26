package org.quickdev.domain.plugin.service;

import java.util.List;
import java.util.Map;

import org.quickdev.domain.plugin.DatasourceMetaInfo;
import org.quickdev.sdk.models.DatasourceConnectionConfig;
import org.quickdev.sdk.plugin.common.DatasourceConnector;
import org.quickdev.sdk.plugin.common.QueryExecutor;
import org.quickdev.sdk.query.QueryExecutionContext;

import reactor.core.publisher.Flux;

public interface DatasourceMetaInfoService {

    DatasourceMetaInfo getDatasourceMetaInfo(String datasourceType);

    /**
     * java based data sources only
     */
    List<DatasourceMetaInfo> getJavaBasedSupportedDatasourceMetaInfos();

    boolean isJavaDatasourcePlugin(String type);

    boolean isJsDatasourcePlugin(String type);

    /**
     * all data sources, include java based, js based...
     */
    Flux<DatasourceMetaInfo> getAllSupportedDatasourceMetaInfos();

    DatasourceConnector<Object, ? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceType);

    QueryExecutor<? extends DatasourceConnectionConfig, Object, ? extends QueryExecutionContext> getQueryExecutor(String datasourceType);

    DatasourceConnectionConfig resolveDetailConfig(Map<String, Object> datasourceDetailMap, String datasourceType);
}
