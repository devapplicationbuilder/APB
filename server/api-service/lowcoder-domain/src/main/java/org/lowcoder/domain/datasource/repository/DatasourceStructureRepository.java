package org.quickdev.domain.datasource.repository;


import org.quickdev.domain.datasource.model.DatasourceStructureDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface DatasourceStructureRepository extends ReactiveMongoRepository<DatasourceStructureDO, String> {

    Mono<DatasourceStructureDO> findByDatasourceId(String datasourceId);

}
