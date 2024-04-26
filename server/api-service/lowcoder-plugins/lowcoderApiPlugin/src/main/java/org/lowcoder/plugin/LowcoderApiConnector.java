package org.quickdev.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.quickdev.sdk.models.DatasourceTestResult;
import org.quickdev.sdk.plugin.common.DatasourceConnector;
import org.quickdev.sdk.plugin.lowcoderapi.LowcoderApiDatasourceConfig;
import org.pf4j.Extension;

import reactor.core.publisher.Mono;

@Extension
public class LowcoderApiConnector implements DatasourceConnector<Object, LowcoderApiDatasourceConfig> {

    private static final Object CONNECTION_OBJECT = new Object();

    @Override
    public Mono<Object> createConnection(LowcoderApiDatasourceConfig connectionConfig) {
        return Mono.just(CONNECTION_OBJECT);
    }

    @Override
    public Mono<Void> destroyConnection(Object o) {
        return Mono.empty();
    }

    @Override
    public Mono<DatasourceTestResult> testConnection(LowcoderApiDatasourceConfig connectionConfig) {
        return Mono.just(DatasourceTestResult.testSuccess());
    }

    @Nonnull
    @Override
    public LowcoderApiDatasourceConfig resolveConfig(Map<String, Object> configMap) {
        return LowcoderApiDatasourceConfig.INSTANCE;
    }

    @Override
    public Set<String> validateConfig(LowcoderApiDatasourceConfig connectionConfig) {
        return Collections.emptySet();
    }

}
