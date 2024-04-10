package org.quickdev.domain.material.repository;

import org.quickdev.domain.material.model.MaterialMeta;
import org.quickdev.domain.material.model.MaterialType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaterialMateRepository extends ReactiveMongoRepository<MaterialMeta, String> {

    Flux<MaterialMeta> findByOrgId(String orgId);

    Flux<MaterialMeta> findByOrgIdAndType(String orgId, MaterialType type);

    Flux<MaterialMeta> findByOrgIdAndFilenameAndType(String orgId, String filename, MaterialType type);

    Mono<Boolean> existsByOrgIdAndFilename(String orgId, String filename);
}
