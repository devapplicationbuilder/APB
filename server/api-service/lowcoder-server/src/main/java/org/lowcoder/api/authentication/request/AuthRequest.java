package org.quickdev.api.authentication.request;

import org.quickdev.domain.authentication.context.AuthRequestContext;
import org.quickdev.domain.user.model.AuthUser;
import reactor.core.publisher.Mono;

/**
 * @see AuthRequestFactory
 */
public interface AuthRequest {

    Mono<AuthUser> auth(AuthRequestContext authRequestContext);

    Mono<AuthUser> refresh(String refreshToken);
}
