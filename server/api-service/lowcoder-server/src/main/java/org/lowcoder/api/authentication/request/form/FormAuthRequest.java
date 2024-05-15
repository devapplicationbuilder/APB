package org.quickdev.api.authentication.request.form;

import org.quickdev.api.authentication.request.AuthRequest;
import org.quickdev.domain.authentication.context.AuthRequestContext;
import org.quickdev.domain.authentication.context.FormAuthRequestContext;
import org.quickdev.domain.encryption.EncryptionService;
import org.quickdev.domain.user.model.AuthUser;
import org.quickdev.domain.user.service.UserService;
import org.quickdev.sdk.auth.AbstractAuthConfig;
import org.quickdev.sdk.auth.EmailAuthConfig;
import org.quickdev.sdk.constants.AuthSourceConstants;
import org.quickdev.sdk.exception.BizError;
import org.quickdev.sdk.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.quickdev.sdk.util.ExceptionUtils.ofError;

@Component
public class FormAuthRequest implements AuthRequest {

    @Autowired
    private UserService userService;
    @Autowired
    private EncryptionService encryptionService;

    @Override
    public Mono<AuthUser> auth(AuthRequestContext authRequestContext) {
        FormAuthRequestContext context = (FormAuthRequestContext) authRequestContext;

        return Mono.defer(() -> {
                    /*
                    AbstractAuthConfig authConfig = context.getAuthConfig();
                    // register
                    if (context.isRegister()) {
                        // register by email
                        if (AuthSourceConstants.EMAIL.equals(authConfig.getSource())
                                && authConfig instanceof EmailAuthConfig emailAuthConfig
                                && emailAuthConfig.isEnableRegister()) {
                            return userService.findBySourceAndId(authConfig.getSource(), context.getLoginId())
                                    .flatMap(user -> ofError(BizError.USER_LOGIN_ID_EXIST, "USER_LOGIN_ID_EXIST"));
                        }
                        // register not by email
                        return Mono.error(new BizException(BizError.UNSUPPORTED_OPERATION, "BAD_REQUEST"));
                    }
                    // login
                    return userService.findBySourceAndId(authConfig.getSource(), context.getLoginId())
                            .switchIfEmpty(ofError(BizError.INVALID_PASSWORD, "INVALID_EMAIL_OR_PASSWORD"))
                            .flatMap(user -> {
                                String raw = context.getPassword();
                                String encoded = user.getPassword();
                                if (!encryptionService.matchPassword(raw, encoded)) {
                                    return ofError(BizError.INVALID_PASSWORD, "INVALID_EMAIL_OR_PASSWORD");
                                }
                                return Mono.empty();
                            });
                    */
                    return Mono.empty();
                })
                .thenReturn(AuthUser.builder().uid(context.getLoginId()).username(context.getLoginId()).build());
    }

    @Override
    public Mono<AuthUser> refresh(String refreshToken) {
        return Mono.error(new UnsupportedOperationException());
    }
}
