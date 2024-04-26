package org.quickdev.infra.config.repository;

import org.quickdev.infra.config.model.ServerConfig;
import org.quickdev.infra.mongo.MongoUpsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
class CustomServerConfigRepositoryImpl implements CustomServerConfigRepository {

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    @Override
    public Mono<ServerConfig> upsert(String key, Object value) {
        ServerConfig newConfig = ServerConfig.builder()
                .key(key)
                .value(value)
                .build();
        return mongoUpsertHelper.upsertWithAuditingParams(newConfig, "key", key);
    }
}
