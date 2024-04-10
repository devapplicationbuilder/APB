package org.quickdev.domain.datasource.service.impl;

import static org.quickdev.domain.datasource.model.TokenBasedConnectionHolder.EMPTY_CONNECTION;
import static org.quickdev.sdk.exception.BizError.DATASOURCE_TYPE_ERROR;
import static org.quickdev.sdk.exception.BizError.PLUGIN_CREATE_CONNECTION_FAILED;
import static org.quickdev.sdk.util.ExceptionUtils.deferredError;

import org.quickdev.domain.datasource.model.Datasource;
import org.quickdev.domain.datasource.model.DatasourceConnectionHolder;
import org.quickdev.domain.datasource.model.TokenBasedConnection;
import org.quickdev.domain.datasource.model.TokenBasedConnectionHolder;
import org.quickdev.domain.datasource.repository.TokenBasedConnectionRepository;
import org.quickdev.domain.datasource.service.DatasourceConnectionPool;
import org.quickdev.domain.plugin.service.DatasourceMetaInfoService;
import org.quickdev.sdk.exception.BizException;
import org.quickdev.sdk.models.TokenBasedConnectionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TokenBasedConnectionPool implements DatasourceConnectionPool {

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    @Autowired
    private TokenBasedConnectionRepository connectionRepository;

    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        String datasourceId = datasource.getId();
        var connectionFactory = datasourceMetaInfoService.getDatasourceConnector(datasource.getType());

        return connectionRepository.findByDatasourceId(datasourceId, datasource.getType())
                .map(TokenBasedConnectionHolder::new)
                .defaultIfEmpty(EMPTY_CONNECTION)
                .flatMap(connection -> {
                    if (connection.isStale(datasource.getUpdatedAt())) {
                        return connectionFactory.doCreateConnection(datasource.getDetailConfig())
                                .switchIfEmpty(deferredError(PLUGIN_CREATE_CONNECTION_FAILED, "DATASOURCE_CONNECT_ERROR"))
                                .flatMap(newConnection -> saveNewConnection(datasource.getId(), newConnection));
                    }

                    return Mono.just(connection);
                });
    }

    @Override
    public Object info(String datasourceId) {
        throw new UnsupportedOperationException();
    }

    private Mono<TokenBasedConnectionHolder> saveNewConnection(String datasourceId, Object connection) {

        if (!(connection instanceof TokenBasedConnectionDetail connectionDetail)) {
            throw new BizException(DATASOURCE_TYPE_ERROR, "DATASOURCE_CONNECTION_TYPE_ERROR", connection.getClass().getSimpleName());
        }

        TokenBasedConnection tokenBasedConnection = new TokenBasedConnection();
        tokenBasedConnection.setDatasourceId(datasourceId);
        tokenBasedConnection.setTokenDetail(connectionDetail);
        return connectionRepository.saveConnection(tokenBasedConnection, datasourceId)
                .thenReturn(new TokenBasedConnectionHolder(tokenBasedConnection));
    }

}

