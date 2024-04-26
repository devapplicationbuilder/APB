package org.quickdev.domain.datasource.service;

import org.quickdev.sdk.models.DatasourceStructure;

import reactor.core.publisher.Mono;

public interface DatasourceStructureService {

    Mono<DatasourceStructure> getStructure(String datasourceId, boolean ignoreCache);

}

