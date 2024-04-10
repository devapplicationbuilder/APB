package org.quickdev.api.authentication.request.form;

import java.util.Set;

import org.quickdev.api.authentication.request.AuthRequest;
import org.quickdev.api.authentication.request.AuthRequestFactory;
import org.quickdev.domain.authentication.context.AuthRequestContext;
import org.quickdev.sdk.auth.constants.AuthTypeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class FormAuthRequestFactory implements AuthRequestFactory<AuthRequestContext> {

    @Autowired
    private FormAuthRequest formAuthRequest;

    @Override
    public Mono<AuthRequest> build(AuthRequestContext context) {
        return Mono.just(formAuthRequest);
    }

    @Override
    public Set<String> supportedAuthTypes() {
        return Set.of(AuthTypeConstants.FORM);
    }
}
