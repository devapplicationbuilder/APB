package org.quickdev.api.home;

import javax.annotation.Nullable;

import org.quickdev.api.application.view.MarketplaceApplicationInfoView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.quickdev.api.application.view.ApplicationInfoView;
import org.quickdev.api.usermanagement.view.UserProfileView;
import org.quickdev.domain.application.model.ApplicationStatus;
import org.quickdev.domain.application.model.ApplicationType;
import org.quickdev.domain.user.model.User;
import org.springframework.web.server.ServerWebExchange;

public interface UserHomeApiService {

    Mono<UserProfileView> buildUserProfileView(User user, ServerWebExchange exchange);

    Mono<Boolean> markNewUserGuidanceShown(String userId);

    Mono<UserHomepageView> getUserHomePageView(ApplicationType applicationType);

    Flux<ApplicationInfoView> getAllAuthorisedApplications4CurrentOrgMember(@Nullable ApplicationType applicationType,
            @Nullable ApplicationStatus applicationStatus, boolean withContainerSize);

    public Flux<MarketplaceApplicationInfoView> getAllMarketplaceApplications(@Nullable ApplicationType applicationType);

    public Flux<MarketplaceApplicationInfoView> getAllAgencyProfileApplications(@Nullable ApplicationType applicationType);
}
