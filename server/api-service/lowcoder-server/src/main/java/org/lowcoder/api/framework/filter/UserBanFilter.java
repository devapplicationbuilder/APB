package org.quickdev.api.framework.filter;

import static org.quickdev.api.framework.filter.FilterOrder.USER_BAN;
import static org.quickdev.sdk.constants.Authentication.isAnonymousUser;
import static org.quickdev.sdk.exception.BizError.USER_BANNED;
import static org.quickdev.sdk.util.ExceptionUtils.ofError;

import javax.annotation.Nonnull;

import org.quickdev.api.home.SessionUserService;
import org.quickdev.domain.user.model.UserStatus;
import org.quickdev.domain.user.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserBanFilter implements WebFilter, Ordered {

    @Autowired
    private UserStatusService userStatusService;

    @Autowired
    private SessionUserService sessionUserService;

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> {
                    if (isAnonymousUser(visitorId)) {
                        return Mono.empty();
                    }

                    return userStatusService.findByUserId(visitorId)
                            .map(UserStatus::isBanned)
                            .defaultIfEmpty(false)
                            .flatMap(isBanned -> {
                                if (isBanned) {
                                    return ofError(USER_BANNED, "USER_BANNED");
                                }
                                return Mono.empty();
                            });
                })
                .then(Mono.defer(() -> chain.filter(exchange)));
    }

    @Override
    public int getOrder() {
        return USER_BAN.getOrder();
    }
}

