package org.quickdev.api.authentication.service;

import org.quickdev.api.authentication.dto.APIKeyRequest;
import org.quickdev.api.authentication.dto.AuthConfigRequest;
import org.quickdev.api.usermanagement.view.APIKeyVO;
import org.quickdev.domain.authentication.FindAuthConfig;
import org.quickdev.domain.user.model.APIKey;
import org.quickdev.domain.user.model.AuthUser;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthenticationApiService {

    Mono<AuthUser> authenticateByForm(String loginId, String password, String source, boolean register, String authId, String orgId, String token);

    Mono<AuthUser> authenticateByOauth2(String authId, String source, String code, String redirectUrl, String orgId);

    Mono<Void> loginOrRegister(AuthUser authUser, ServerWebExchange exchange, String invitationId, boolean linKExistingUser);

    Mono<Boolean> enableAuthConfig(AuthConfigRequest authConfigRequest);

    Mono<Boolean> disableAuthConfig(String authId, boolean delete);

    Flux<FindAuthConfig> findAuthConfigs(boolean enableOnly);

    Mono<APIKeyVO> createAPIKey(APIKeyRequest apiKeyRequest);

    Mono<Void> deleteAPIKey(String authId);

    Flux<APIKey> findAPIKeys();
}
