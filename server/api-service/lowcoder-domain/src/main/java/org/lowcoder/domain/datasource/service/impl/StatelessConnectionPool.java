package org.quickdev.domain.datasource.service.impl;

import org.quickdev.domain.datasource.model.Datasource;
import org.quickdev.domain.datasource.model.DatasourceConnectionHolder;
import org.quickdev.domain.datasource.model.StatelessDatasourceConnectionHolder;
import org.quickdev.domain.datasource.service.DatasourceConnectionPool;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class StatelessConnectionPool implements DatasourceConnectionPool {

    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        return Mono.just(new StatelessDatasourceConnectionHolder());
    }

    @Override
    public Object info(String datasourceId) {
        throw new UnsupportedOperationException();
    }
}

