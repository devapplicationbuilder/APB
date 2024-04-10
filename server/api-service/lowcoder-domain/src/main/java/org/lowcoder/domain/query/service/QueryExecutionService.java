package org.quickdev.domain.query.service;

import lombok.extern.slf4j.Slf4j;
import org.quickdev.domain.datasource.model.Datasource;
import org.quickdev.domain.datasource.model.DatasourceConnectionHolder;
import org.quickdev.domain.datasource.service.DatasourceConnectionPool;
import org.quickdev.domain.plugin.client.DatasourcePluginClient;
import org.quickdev.domain.plugin.service.DatasourceMetaInfoService;
import org.quickdev.domain.query.util.QueryTimeoutUtils;
import org.quickdev.sdk.config.CommonConfig;
import org.quickdev.sdk.exception.BizException;
import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.models.JsDatasourceConnectionConfig;
import org.quickdev.sdk.models.Property;
import org.quickdev.sdk.models.QueryExecutionResult;
import org.quickdev.sdk.query.QueryExecutionContext;
import org.quickdev.sdk.query.QueryVisitorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.quickdev.sdk.exception.BizError.QUERY_EXECUTION_ERROR;
import static org.quickdev.sdk.exception.PluginCommonError.QUERY_EXECUTION_TIMEOUT;
import static org.quickdev.sdk.util.ExceptionUtils.ofException;

@Slf4j
@Service
public class QueryExecutionService {

    @Autowired
    private DatasourceConnectionPool datasourceConnectionPool;

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    @Autowired
    private CommonConfig common;
    
    public Mono<QueryExecutionResult> executeQuery(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams,
            String timeoutStr, QueryVisitorContext queryVisitorContext) {

        int timeoutMs = QueryTimeoutUtils.parseQueryTimeoutMs(timeoutStr, requestParams, common.getMaxQueryTimeout());
        queryConfig.putIfAbsent("timeoutMs", String.valueOf(timeoutMs));

        return Mono.defer(() -> {
                    if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
                        return executeByNodeJs(datasource, queryConfig, requestParams, queryVisitorContext);
                    }
                    return executeLocally(datasource, queryConfig, requestParams, queryVisitorContext);
                })
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorMap(TimeoutException.class, e -> new PluginException(QUERY_EXECUTION_TIMEOUT, "PLUGIN_EXECUTION_TIMEOUT", timeoutMs))
                .onErrorResume(PluginException.class, pluginException -> Mono.just(QueryExecutionResult.error(pluginException)))
                .onErrorMap(exception -> {
                    if (exception instanceof BizException) {
                        return exception;
                    }
                    log.error("query exception", exception);
                    return ofException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", exception.getMessage());
                });
    }

    private Mono<QueryExecutionResult> executeLocally(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams,
            QueryVisitorContext queryVisitorContext) {
        var queryExecutor = datasourceMetaInfoService.getQueryExecutor(datasource.getType());

        return queryExecutor.buildQueryExecutionContextMono(datasource.getDetailConfig(), queryConfig, requestParams, queryVisitorContext)
                .zipWhen(context -> datasourceConnectionPool.getOrCreateConnection(datasource))
                .flatMap(tuple -> {
                    QueryExecutionContext queryExecutionRequest = tuple.getT1();
                    DatasourceConnectionHolder connectionHolder = tuple.getT2();
                    return queryExecutor.doExecuteQuery(connectionHolder.connection(), queryExecutionRequest)
                            .doOnError(connectionHolder::onQueryError);
                });
    }

    private Mono<QueryExecutionResult> executeByNodeJs(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        List<Map<String, Object>> context = requestParams.entrySet()
                .stream()
                .map(entry -> Map.of("key", entry.getKey(), "value", entry.getValue()))
                .collect(Collectors.toList());

        //forward cookies to js datasource
        List<Map<String, Object>> cookies = queryVisitorContext.getCookies().entrySet()
                .stream()
                .map(entry -> Map.of("key", entry.getKey(), "value", entry.getValue()))
                .collect(Collectors.toList());
        context.addAll(cookies);

        // forward oauth2 access token in case of oauth2(inherit from login)

        if(datasource.getDetailConfig() instanceof JsDatasourceConnectionConfig jsDatasourceConnectionConfig
                && jsDatasourceConnectionConfig.isOauth2InheritFromLogin()) {
            return Mono.defer(() -> injectOauth2Token(queryVisitorContext, context))
                    .then(Mono.defer(() -> datasourcePluginClient.executeQuery(datasource.getType(), queryConfig, context, datasource.getDetailConfig())));
        } else {
            return datasourcePluginClient.executeQuery(datasource.getType(), queryConfig, context, datasource.getDetailConfig());
        }


    }

    private Mono<Void> injectOauth2Token(QueryVisitorContext queryVisitorContext, List<Map<String, Object>> context) {
        return queryVisitorContext.getAuthTokenMono()
                .doOnNext(properties -> {
                    for (Property property : properties) {
                        context.add(Map.of("key" , property.getKey(), "value", property.getValue()));
                    }
                })
                .then();
    }
}
