package org.quickdev.plugin.restapi;

import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.quickdev.sdk.models.DatasourceTestResult;
import org.quickdev.sdk.plugin.common.DatasourceConnector;
import org.quickdev.sdk.plugin.restapi.RestApiDatasourceConfig;
import org.pf4j.Extension;

import reactor.core.publisher.Mono;

@Extension
public class RestApiConnector implements DatasourceConnector<Object, RestApiDatasourceConfig> {

    @Override
    public Mono<Object> createConnection(RestApiDatasourceConfig connectionConfig) {
        return Mono.just(new Object());
    }

    @Override
    public Set<String> validateConfig(RestApiDatasourceConfig connectionConfig) {
        return emptySet();
    }

    @Override
    public Mono<DatasourceTestResult> testConnection(RestApiDatasourceConfig connectionConfig) {
        return Mono.just(DatasourceTestResult.testSuccess());
    }

    @Override
    public Mono<Void> destroyConnection(Object connection) {
        return Mono.empty();
    }

    @Nonnull
    @Override
    public RestApiDatasourceConfig resolveConfig(Map<String, Object> configMap) {
        return RestApiDatasourceConfig.buildFrom(configMap);
    }

}