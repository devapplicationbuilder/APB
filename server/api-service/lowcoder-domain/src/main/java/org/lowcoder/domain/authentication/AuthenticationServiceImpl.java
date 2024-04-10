package org.quickdev.domain.authentication;

import lombok.extern.slf4j.Slf4j;
import org.quickdev.domain.organization.service.OrgMemberService;
import org.quickdev.domain.organization.service.OrganizationService;
import org.quickdev.sdk.auth.AbstractAuthConfig;
import org.quickdev.sdk.auth.EmailAuthConfig;
import org.quickdev.sdk.config.AuthProperties;
import org.quickdev.sdk.config.CommonConfig;
import org.quickdev.sdk.constants.AuthSourceConstants;
import org.quickdev.sdk.constants.WorkspaceMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.quickdev.sdk.exception.BizError.LOG_IN_SOURCE_NOT_SUPPORTED;
import static org.quickdev.sdk.util.ExceptionUtils.ofError;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrgMemberService orgMemberService;

    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private AuthProperties authProperties;

    @Override
    public Mono<FindAuthConfig> findAuthConfigByAuthId(String orgId, String authId) {
        return findAuthConfig(orgId, abstractAuthConfig -> Objects.equals(authId, abstractAuthConfig.getId()));
    }

    @Override
    @Deprecated
    public Mono<FindAuthConfig> findAuthConfigBySource(String orgId, String source) {
        return findAuthConfig(orgId, abstractAuthConfig -> Objects.equals(source, abstractAuthConfig.getSource()));
    }

    private Mono<FindAuthConfig> findAuthConfig(String orgId, Function<AbstractAuthConfig, Boolean> condition) {
        return findAllAuthConfigs(orgId,true)
                .filter(findAuthConfig -> condition.apply(findAuthConfig.authConfig()))
                .next()
                .switchIfEmpty(ofError(LOG_IN_SOURCE_NOT_SUPPORTED, "LOG_IN_SOURCE_NOT_SUPPORTED"));
    }

    @Override
    public Flux<FindAuthConfig> findAllAuthConfigs(String orgId, boolean enableOnly) {

        Mono<FindAuthConfig> emailAuthConfigMono = orgMemberService.doesAtleastOneAdminExist()
                .map(doesAtleastOneAdminExist -> {
                    boolean shouldEnableRegister;
                    if(doesAtleastOneAdminExist) {
                        shouldEnableRegister = authProperties.getEmail().getEnableRegister();
                    } else {
                        shouldEnableRegister = Boolean.TRUE;
                    }
                    return new FindAuthConfig
                            (new EmailAuthConfig(AuthSourceConstants.EMAIL, authProperties.getEmail().isEnable(), shouldEnableRegister), null);
                });


        Flux<FindAuthConfig> findAuthConfigFlux = findAllAuthConfigsByDomain()
                .switchIfEmpty(findAllAuthConfigsForEnterpriseMode())
                .switchIfEmpty(findAllAuthConfigsForSaasMode(orgId))
                .filter(findAuthConfig -> {
                    if (enableOnly) {
                        return findAuthConfig.authConfig().isEnable();
                    }
                    return true;
                });

        return Flux.concat(findAuthConfigFlux, emailAuthConfigMono);

    }

    private Flux<FindAuthConfig> findAllAuthConfigsByDomain() {
        return organizationService.getByDomain()
                .flatMapIterable(organization ->
                        organization.getAuthConfigs()
                                .stream()
                                .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                                .collect(Collectors.toList())
                );
    }

    protected Flux<FindAuthConfig> findAllAuthConfigsForEnterpriseMode() {
        if (commonConfig.getWorkspace().getMode() == WorkspaceMode.SAAS) {
            return Flux.empty();
        }
        return organizationService.getOrganizationInEnterpriseMode()
                .flatMapIterable(organization ->
                        organization.getAuthConfigs()
                                .stream()
                                .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                                .collect(Collectors.toList())
                );
    }

    private Flux<FindAuthConfig> findAllAuthConfigsForSaasMode(String orgId) {
        if (commonConfig.getWorkspace().getMode() == WorkspaceMode.SAAS) {

            // Get the auth configs for the current org
            if(orgId != null) {
                return organizationService.getById(orgId)
                        .flatMapIterable(organization ->
                                organization.getAuthConfigs()
                                        .stream()
                                        .map(abstractAuthConfig -> new FindAuthConfig(abstractAuthConfig, organization))
                                        .collect(Collectors.toList())
                        );
            }

        }
        return Flux.empty();
    }
}
