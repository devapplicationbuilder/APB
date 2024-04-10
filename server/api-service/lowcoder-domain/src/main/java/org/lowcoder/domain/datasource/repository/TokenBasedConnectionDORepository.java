package org.quickdev.domain.datasource.repository;

import org.quickdev.domain.datasource.model.TokenBasedConnectionDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;


public interface TokenBasedConnectionDORepository extends ReactiveMongoRepository<TokenBasedConnectionDO, String> {

    Mono<TokenBasedConnectionDO> findByDatasourceId(String datasourceId);
}
