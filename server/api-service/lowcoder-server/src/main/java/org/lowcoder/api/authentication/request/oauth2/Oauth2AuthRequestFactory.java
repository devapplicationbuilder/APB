package org.quickdev.api.authentication.request.oauth2;

import java.util.Set;

import org.quickdev.api.authentication.request.AuthRequest;
import org.quickdev.api.authentication.request.AuthRequestFactory;
import org.quickdev.api.authentication.request.oauth2.request.AbstractOauth2Request;
import org.quickdev.api.authentication.request.oauth2.request.GithubRequest;
import org.quickdev.api.authentication.request.oauth2.request.GoogleRequest;
import org.quickdev.api.authentication.request.oauth2.request.KeycloakRequest;
import org.quickdev.api.authentication.request.oauth2.request.OryRequest;
import org.quickdev.sdk.auth.Oauth2KeycloakAuthConfig;
import org.quickdev.sdk.auth.Oauth2OryAuthConfig;
import org.quickdev.sdk.auth.Oauth2SimpleAuthConfig;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import static org.quickdev.sdk.auth.constants.AuthTypeConstants.*;

@Component
public class Oauth2AuthRequestFactory implements AuthRequestFactory<OAuth2RequestContext> {

    @Override
    public Mono<AuthRequest> build(OAuth2RequestContext context) {
        return Mono.fromSupplier(() -> buildRequest(context));
    }

    private AbstractOauth2Request<? extends Oauth2SimpleAuthConfig> buildRequest(OAuth2RequestContext context) {
        return switch (context.getAuthConfig().getAuthType()) {
            case GITHUB -> new GithubRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            case GOOGLE -> new GoogleRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            case ORY -> new OryRequest((Oauth2OryAuthConfig) context.getAuthConfig());
            case KEYCLOAK -> new KeycloakRequest((Oauth2KeycloakAuthConfig)context.getAuthConfig());
            default -> throw new UnsupportedOperationException(context.getAuthConfig().getAuthType());
        };
    }

    @Override
    public Set<String> supportedAuthTypes() {
        return Set.of(
                GITHUB,
                GOOGLE,
                ORY,
                KEYCLOAK);
    }
}
