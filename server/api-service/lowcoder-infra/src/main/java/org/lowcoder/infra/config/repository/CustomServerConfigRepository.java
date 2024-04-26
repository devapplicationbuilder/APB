package org.quickdev.infra.config.repository;

import org.quickdev.infra.config.model.ServerConfig;

import reactor.core.publisher.Mono;

interface CustomServerConfigRepository {

    Mono<ServerConfig> upsert(String key, Object value);
}
