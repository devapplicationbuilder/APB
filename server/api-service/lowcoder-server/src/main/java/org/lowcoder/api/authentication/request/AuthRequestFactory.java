package org.quickdev.api.authentication.request;

import java.util.Set;

import org.quickdev.domain.authentication.context.AuthRequestContext;

import reactor.core.publisher.Mono;

public interface AuthRequestFactory<T extends AuthRequestContext> {

    Mono<AuthRequest> build(T context);

    Set<String> supportedAuthTypes();
}
