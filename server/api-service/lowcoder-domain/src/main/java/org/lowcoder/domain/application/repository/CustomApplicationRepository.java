package org.quickdev.domain.application.repository;

import org.quickdev.domain.application.model.Application;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomApplicationRepository {

    Flux<Application> findByOrganizationIdWithDsl(String organizationId);

    Mono<Application> findByIdWithDsl(String applicationId);
}
