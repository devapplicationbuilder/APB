package org.quickdev.api.authentication.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.quickdev.api.authentication.dto.APIKeyRequest;
import org.quickdev.api.authentication.dto.AuthConfigRequest;
import org.quickdev.api.authentication.request.AuthRequestFactory;
import org.quickdev.api.authentication.request.oauth2.OAuth2RequestContext;
import org.quickdev.api.authentication.service.factory.AuthConfigFactory;
import org.quickdev.api.authentication.util.AuthenticationUtils;
import org.quickdev.api.authentication.util.JWTUtils;
import org.quickdev.api.home.SessionUserService;
import org.quickdev.api.usermanagement.InvitationApiService;
import org.quickdev.api.usermanagement.OrgApiService;
import org.quickdev.api.usermanagement.UserApiService;
import org.quickdev.api.usermanagement.view.APIKeyVO;
import org.quickdev.api.util.BusinessEventPublisher;
import org.quickdev.domain.authentication.AuthenticationService;
import org.quickdev.domain.authentication.FindAuthConfig;
import org.quickdev.domain.authentication.context.AuthRequestContext;
import org.quickdev.domain.authentication.context.FormAuthRequestContext;
import org.quickdev.domain.organization.model.OrgMember;
import org.quickdev.domain.organization.model.Organization;
import org.quickdev.domain.organization.model.OrganizationDomain;
import org.quickdev.domain.organization.service.OrgMemberService;
import org.quickdev.domain.organization.service.OrganizationService;
import org.quickdev.domain.user.model.*;
import org.quickdev.domain.user.service.UserService;
import org.quickdev.sdk.auth.AbstractAuthConfig;
import org.quickdev.sdk.config.AuthProperties;
import org.quickdev.sdk.exception.BizError;
import org.quickdev.sdk.exception.BizException;
import org.quickdev.sdk.util.CookieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.quickdev.sdk.exception.BizError.*;
import static org.quickdev.sdk.util.ExceptionUtils.deferredError;
import static org.quickdev.sdk.util.ExceptionUtils.ofError;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.quickdev.domain.group.service.GroupService;
import java.util.Locale;
import org.quickdev.domain.group.model.Group;
import org.quickdev.domain.organization.model.MemberRole;
import org.quickdev.domain.group.service.GroupMemberService;

@Service
@Slf4j
public class AuthenticationApiServiceImpl implements AuthenticationApiService {

    @Autowired
    private OrgApiService orgApiService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AuthRequestFactory<AuthRequestContext> authRequestFactory;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;
    @Autowired
    private InvitationApiService invitationApiService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;
    @Autowired
    private SessionUserService sessionUserService;
    @Autowired
    private CookieHelper cookieHelper;
    @Autowired
    private AuthConfigFactory authConfigFactory;
    @Autowired
    private UserApiService userApiService;
    @Autowired
    private OrgMemberService orgMemberService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthProperties authProperties;

    private final WebClient webClient;

    public AuthenticationApiServiceImpl() {
        this.webClient = WebClient.create();
    }

    @Override
    public Mono<AuthUser> authenticateByForm(String loginId, String password, String source, boolean register, String authId, String orgId, String token, String authType, @RequestHeader HttpHeaders headers) {
        String oamRemoteUser = headers.getFirst("OAM_REMOTE_USER");
        String oamRemoteUserAlt = headers.getFirst("OAM-REMOTE-USER");
    
        if ((oamRemoteUser == null || oamRemoteUser.isEmpty()) && (oamRemoteUserAlt == null || oamRemoteUserAlt.isEmpty())) {
	        return deferredError(BizError.OAM_REMOTE_USER_MISSING, "OAM_REMOTE_USER_MISSING");
        }

        String ldapUser = oamRemoteUser == null || oamRemoteUser.isEmpty() ? oamRemoteUserAlt : oamRemoteUser;
        String ldapPassword = "123456";

	    return authenticate(authId, source, new FormAuthRequestContext(ldapUser, ldapPassword, true, orgId));

        //return authenticate(authId, source, new FormAuthRequestContext(loginId, password, register, orgId));
    }

    @Override
    public Mono<AuthUser> authenticateByOauth2(String authId, String source, String code, String redirectUrl, String orgId) {
        return authenticate(authId, source, new OAuth2RequestContext(orgId, code, redirectUrl));
    }

    protected Mono<AuthUser> authenticate(String authId, @Deprecated String source, AuthRequestContext context) {
        return Mono.defer(() -> {
                    if (StringUtils.isNotBlank(authId)) {
                        return authenticationService.findAuthConfigByAuthId(context.getOrgId(), authId);
                    }
                    log.warn("source is deprecated and will be removed in the future, please use authId instead. {}", source);
                    return authenticationService.findAuthConfigBySource(context.getOrgId(), source);
                })
                .doOnNext(findAuthConfig -> {
                    context.setAuthConfig(findAuthConfig.authConfig());
                    if (findAuthConfig.authConfig().getSource().equals("EMAIL")) {
                        if(StringUtils.isBlank(context.getOrgId())) {
                            context.setOrgId(Optional.ofNullable(findAuthConfig.organization()).map(Organization::getId).orElse(null));
                        }
                    } else {
                        context.setOrgId(Optional.ofNullable(findAuthConfig.organization()).map(Organization::getId).orElse(null));
                    }
                })
                .then(authRequestFactory.build(context))
                .flatMap(authRequest -> authRequest.auth(context))
                .doOnNext(authorizedUser -> {
                    authorizedUser.setOrgId(context.getOrgId());
                    authorizedUser.setAuthContext(context);
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof BizException) {
                        return Mono.error(throwable);
                    }
                    log.error("user auth error.", throwable);
                    return ofError(AUTH_ERROR, "AUTH_ERROR");
                });
    }

    @Override
    public Mono<Void> loginOrRegisterLdap(AuthUser authUser, ServerWebExchange exchange,
                                      String invitationId, boolean linKExistingUser, String groups) {
        return updateOrCreateUser(authUser, linKExistingUser)
                .delayUntil(user -> ReactiveSecurityContextHolder.getContext()
                        .doOnNext(securityContext -> securityContext.setAuthentication(AuthenticationUtils.toAuthentication(user))))
                // save token and set cookie
                .delayUntil(user -> {
                    String token = CookieHelper.generateCookieToken();
                    return sessionUserService.saveUserSession(token, user, authUser.getSource())
                            .then(Mono.fromRunnable(() -> cookieHelper.saveCookie(token, exchange)));
                })
                // after register
                .delayUntil(user -> {
                    boolean createWorkspace =
                            authUser.getOrgId() == null && StringUtils.isBlank(invitationId) && authProperties.getWorkspaceCreation();
                    if (user.getIsNewUser() && createWorkspace) {
                        return onUserRegisterLdap(user, groups);
                    }
                    return Mono.empty();
                })
                // after login
                .delayUntil(user -> onUserLoginLdap(authUser.getOrgId(), user, authUser.getSource(), groups))
                // process invite
                .delayUntil(__ -> {
                    if (StringUtils.isBlank(invitationId)) {
                        return Mono.empty();
                    }
                    return invitationApiService.inviteUser(invitationId);
                })
                // publish event
                .then(businessEventPublisher.publishUserLoginEvent(authUser.getSource()));
    }

    @Override
    public Mono<Void> loginOrRegister(AuthUser authUser, ServerWebExchange exchange,
                                      String invitationId, boolean linKExistingUser) {
        return updateOrCreateUser(authUser, linKExistingUser)
                .delayUntil(user -> ReactiveSecurityContextHolder.getContext()
                        .doOnNext(securityContext -> securityContext.setAuthentication(AuthenticationUtils.toAuthentication(user))))
                // save token and set cookie
                .delayUntil(user -> {
                    String token = CookieHelper.generateCookieToken();
                    return sessionUserService.saveUserSession(token, user, authUser.getSource())
                            .then(Mono.fromRunnable(() -> cookieHelper.saveCookie(token, exchange)));
                })
                // after register
                .delayUntil(user -> {
                    boolean createWorkspace =
                            authUser.getOrgId() == null && StringUtils.isBlank(invitationId) && authProperties.getWorkspaceCreation();
                    if (user.getIsNewUser() && createWorkspace) {
                        return onUserRegister(user);
                    }
                    return Mono.empty();
                })
                // after login
                .delayUntil(user -> onUserLogin(authUser.getOrgId(), user, authUser.getSource()))
                // process invite
                .delayUntil(__ -> {
                    if (StringUtils.isBlank(invitationId)) {
                        return Mono.empty();
                    }
                    return invitationApiService.inviteUser(invitationId);
                })
                // publish event
                .then(businessEventPublisher.publishUserLoginEvent(authUser.getSource()));
    }

    private Mono<User> updateOrCreateUser(AuthUser authUser, boolean linkExistingUser) {

        if(linkExistingUser) {
            return sessionUserService.getVisitor()
                    .flatMap(user -> userService.addNewConnectionAndReturnUser(user.getId(), authUser.toAuthConnection()));
        }

        return findByAuthUserSourceAndRawId(authUser).zipWith(findByAuthUserRawId(authUser))
                .delayUntil(user -> orgApiService.checkLicenseValid(authUser.getOrgId()))
                .flatMap(tuple -> {

                    FindByAuthUser findByAuthUserFirst = tuple.getT1();
                    FindByAuthUser findByAuthUserSecond = tuple.getT2();

                    // If the user is found for the same auth source and id, just update the connection
                    if (findByAuthUserFirst.userExist()) {
                        User user = findByAuthUserFirst.user();
                        updateConnection(authUser, user);
                        return userService.update(user.getId(), user);
                    }

                    //If the user connection is not found with login id, but the user is
                    // found for the same id in some different connection, then just add a new connection to the user
                    if(findByAuthUserSecond.userExist()) {
                        User user = findByAuthUserSecond.user();
                        return userService.addNewConnectionAndReturnUser(user.getId(), authUser.toAuthConnection());
                    }

                    // if the user is logging/registering via OAuth provider for the first time,
                    // but is not anonymous, then just add a new connection

                     userService.findById(authUser.getUid())
                             .switchIfEmpty(Mono.empty())
                             .filter(user -> {
                                 // not logged in yet
                                 return !user.isAnonymous();
                             }).doOnNext(user -> {
                                 userService.addNewConnection(user.getId(), authUser.toAuthConnection());
                             }).subscribe();


                    if (authUser.getAuthContext().getAuthConfig().isEnableRegister()) {
                        if(authUser.getOrgId() == null)
                            return userService.createNewUserByAuthUser(authUser);
                        return userService.createNewUserByAuthUser(authUser)
                                .delayUntil(user -> orgApiService.checkMaxOrgMemberCount(authUser.getOrgId()));
                    }
                    return Mono.error(new BizException(USER_NOT_EXIST, "USER_NOT_EXIST"));
                });
    }

    protected Mono<FindByAuthUser> findByAuthUserSourceAndRawId(AuthUser authUser) {
        return userService.findByAuthUserSourceAndRawId(authUser)
                .map(user -> new FindByAuthUser(true, user))
                .defaultIfEmpty(new FindByAuthUser(false, null));
    }

    protected Mono<FindByAuthUser> findByAuthUserRawId(AuthUser authUser) {
        return userService.findByAuthUserRawId(authUser)
                .map(user -> new FindByAuthUser(true, user))
                .defaultIfEmpty(new FindByAuthUser(false, null));
    }

    /**
     * Update the connection after re-authenticating
     */
    public void updateConnection(AuthUser authUser, User user) {

        String orgId = authUser.getOrgId();
        Connection oldConnection = getAuthConnection(authUser, user);
        if (StringUtils.isNotBlank(orgId) && !oldConnection.containOrg(orgId)) {  // already exist in user auth connection
            oldConnection.addOrg(orgId);
        }
        // clean old data
        oldConnection.setAuthId(authUser.getAuthContext().getAuthConfig().getId());

        // Save the auth token which may be used in the future datasource or query.
        oldConnection.setAuthConnectionAuthToken(
                Optional.ofNullable(authUser.getAuthToken()).map(ConnectionAuthToken::of).orElse(null));
        oldConnection.setRawUserInfo(authUser.getRawUserInfo());

        user.setActiveAuthId(oldConnection.getAuthId());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected Connection getAuthConnection(AuthUser authUser, User user) {
        return user.getConnections()
                .stream()
                .filter(connection -> authUser.getSource().equals(connection.getSource())
                        && connection.getRawId().equals(authUser.getUid()))
                .findFirst()
                .get();
    }

    private Mono<Void> onUserRegisterLdap(User user, String groups) {
        return organizationService.createDefault(user)
            .flatMap(org -> {
                return processGroups(user.getId(), org.getId(), groups);
            });
    }

    private Mono<Void> onUserLoginLdap(String orgId, User user, String source, String groups) {
        if (StringUtils.isEmpty(orgId)) {
            return Mono.empty();
        }

        return orgApiService.tryAddUserToOrgAndSwitchOrg(orgId, user.getId())
            .flatMap(ignored -> {
                return processGroups(user.getId(), orgId, groups);
            });
    }

    private Mono<Void> processGroups(String userId, String orgId, String groups) {
        return groupService.getByOrgId(orgId)
            .filter(group -> !group.isSystemGroup())
            .flatMap(group -> {
                String[] ldapGroups = groups.split("\\|\\|\\|");
                return Flux.fromArray(ldapGroups)
                    .any(ldapGroup -> ldapGroup.equals(group.getName(Locale.ENGLISH)))
                    .flatMap(belongsToLdapGroup -> {
                        if (belongsToLdapGroup) {
                            return groupMemberService.isMember(group, userId)
                                .flatMap(isMember -> {
                                    if (!isMember) {
                                        return groupMemberService.addMember(orgId, group.getId(), userId, MemberRole.MEMBER)
                                            .then(Mono.just(true));
                                    }
                                    return Mono.just(false);
                                });
                        } else {
                            return groupMemberService.isMember(group, userId)
                                .flatMap(isMember -> {
                                    if (isMember) {
                                        return groupMemberService.removeMember(group.getId(), userId)
                                            .then(Mono.just(true));
                                    }
                                    return Mono.just(false);
                                });
                        }
                    });
            })
            .then();
    }


    protected Mono<Void> onUserRegister(User user) {
        return organizationService.createDefault(user).then();
    }

    protected Mono<Void> onUserLogin(String orgId, User user, String source) {

        if (StringUtils.isEmpty(orgId)) {
            return Mono.empty();
        }
        return orgApiService.tryAddUserToOrgAndSwitchOrg(orgId, user.getId()).then();
    }

    @Override
    public Mono<Boolean> enableAuthConfig(AuthConfigRequest authConfigRequest) {
        return checkIfAdmin()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> organizationService.getById(orgMember.getOrgId()))
                .doOnNext(organization -> {
                    boolean duplicateAuthType = addOrUpdateNewAuthConfig(organization, authConfigFactory.build(authConfigRequest, true));
                    if(duplicateAuthType) {
                        deferredError(DUPLICATE_AUTH_CONFIG_ADDITION, "DUPLICATE_AUTH_CONFIG_ADDITION");
                    }
                })
                .flatMap(organization -> organizationService.update(organization.getId(), organization));
    }

    @Override
    public Mono<Boolean> disableAuthConfig(String authId, boolean delete) {
        return checkIfAdmin()
                .then(checkIfOnlyEffectiveCurrentUserConnections(authId))
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> organizationService.getById(orgMember.getOrgId()))
                .doOnNext(organization -> disableAuthConfig(organization, authId, delete))
                .flatMap(organization -> organizationService.update(organization.getId(), organization))
                .delayUntil(result -> {
                    if (result) {
                        return removeTokensByAuthId(authId);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Flux<FindAuthConfig> findAuthConfigs(boolean enableOnly) {
        return checkIfAdmin().
                then(sessionUserService.getVisitorOrgMemberCache())
                .flatMapMany(orgMember -> authenticationService.findAllAuthConfigs(orgMember.getOrgId(),false));
    }

    @Override
    public Mono<APIKeyVO> createAPIKey(APIKeyRequest apiKeyRequest) {
        return sessionUserService.getVisitor()
                .map(user -> {
                    String token = jwtUtils.createToken(user);
                    APIKey apiKey = new APIKey(apiKeyRequest.getId(), apiKeyRequest.getName(), apiKeyRequest.getDescription(), token);
                    addAPIKey(user, apiKey);
                    return Pair.of(APIKey.builder().id(apiKey.getId()).token(token).build(), user);
                })
                .flatMap(pair -> userService.update(pair.getRight().getId(), pair.getRight()).thenReturn(pair.getKey()))
                .map(APIKeyVO::from);
    }

    private void addAPIKey(User user, APIKey newApiKey) {
        Map<String, APIKey> apiKeyMap = user.getApiKeysList()
                .stream()
                .collect(Collectors.toMap(APIKey::getId, Function.identity()));
        apiKeyMap.put(newApiKey.getId(), newApiKey);
        user.setApiKeysList(new ArrayList<>(apiKeyMap.values()));
    }

    @Override
    public Mono<Void> deleteAPIKey(String apiKeyId) {
        return sessionUserService.getVisitor()
                .doOnNext(user -> deleteAPIKey(user, apiKeyId))
                .flatMap(user -> userService.update(user.getId(), user))
                .then();
    }

    private void deleteAPIKey(User user, String apiKeyId) {
        List<APIKey> apiKeys = Optional.of(user)
                .map(User::getApiKeysList)
                .orElse(Collections.emptyList());
        apiKeys.removeIf(apiKey -> Objects.equals(apiKey.getId(), apiKeyId));
        user.setApiKeysList(apiKeys);
    }

    @Override
    public Flux<APIKey> findAPIKeys() {
        return sessionUserService.getVisitor()
                .flatMapIterable(user ->
                        new ArrayList<>(user.getApiKeysList())
                );
    }


    private Mono<Void> removeTokensByAuthId(String authId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMapMany(orgMember -> orgMemberService.getOrganizationMembers(orgMember.getOrgId()))
                .map(OrgMember::getUserId)
                .flatMap(userId -> userApiService.getTokensByAuthId(userId, authId))
                .delayUntil(token -> sessionUserService.removeUserSession(token))
                .then();
    }

    private Mono<Void> checkIfAdmin() {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (orgMember.isAdmin()) {
                        return Mono.empty();
                    }
                    return deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED");
                });
    }

    /**
     * Check if the auth config identified by the source means the only effective connection for the current user whom should be an administrator.
     * If true, throw an exception to avoid disabling the last effective connection way.
     */
    private Mono<Void> checkIfOnlyEffectiveCurrentUserConnections(String authId) {
        Mono<List<String>> userConnectionAuthConfigIdListMono = sessionUserService.getVisitor()
                .flatMapIterable(User::getConnections)
                .filter(connection -> StringUtils.isNotBlank(connection.getAuthId()))
                .map(Connection::getAuthId)
                .collectList();
        Mono<List<String>> orgAuthIdListMono = authenticationService.findAllAuthConfigs(null, true)
                .map(FindAuthConfig::authConfig)
                .map(AbstractAuthConfig::getId)
                .collectList();
        return Mono.zip(userConnectionAuthConfigIdListMono, orgAuthIdListMono)
                .delayUntil(tuple -> {
                    List<String> userConnectionAuthConfigIds = tuple.getT1();
                    List<String> orgAuthConfigIds = tuple.getT2();
                    userConnectionAuthConfigIds.retainAll(orgAuthConfigIds);
                    userConnectionAuthConfigIds.remove(authId);
                    if (CollectionUtils.isEmpty(userConnectionAuthConfigIds)) {
                        return Mono.error(new BizException(DISABLE_AUTH_CONFIG_FORBIDDEN, "DISABLE_AUTH_CONFIG_FORBIDDEN"));
                    }
                    return Mono.empty();
                })
                .then();
    }

    private void disableAuthConfig(Organization organization, String authId, boolean delete) {

        Predicate<AbstractAuthConfig> authConfigPredicate = abstractAuthConfig -> Objects.equals(abstractAuthConfig.getId(), authId);

        if(delete) {
            List<AbstractAuthConfig> abstractAuthConfigs = Optional.of(organization)
                    .map(Organization::getAuthConfigs)
                    .orElse(Collections.emptyList());

            abstractAuthConfigs.removeIf(authConfigPredicate);

            organization.getOrganizationDomain().setConfigs(abstractAuthConfigs);

        } else {
            Optional.of(organization)
                    .map(Organization::getAuthConfigs)
                    .orElse(Collections.emptyList()).stream()
                    .filter(authConfigPredicate)
                    .forEach(abstractAuthConfig -> {
                        abstractAuthConfig.setEnable(false);
                    });
        }
    }

    /**
     * If the source of the newAuthConfig exists in the auth configs of the organization, update it. Otherwise, add it.
     */
    private boolean addOrUpdateNewAuthConfig(Organization organization, AbstractAuthConfig newAuthConfig) {
        OrganizationDomain organizationDomain = organization.getOrganizationDomain();
        if (organizationDomain == null) {
            organizationDomain = new OrganizationDomain();
            organization.setOrganizationDomain(organizationDomain);
        }

        Map<String, AbstractAuthConfig> authConfigMap = organizationDomain.getConfigs()
                .stream()
                .collect(Collectors.toMap(AbstractAuthConfig::getId, Function.identity()));

        boolean authTypeAlreadyExists = authConfigMap.values().stream()
                .anyMatch(config -> !config.getId().equals(newAuthConfig.getId()) && config.getAuthType().equals(newAuthConfig.getAuthType()));
        if(authTypeAlreadyExists) {
            return false;
        }

        // Under the organization, the source can uniquely identify the whole auth config.
        AbstractAuthConfig old = authConfigMap.get(newAuthConfig.getId());
        if (old != null) {
            newAuthConfig.merge(old);
        }
        authConfigMap.put(newAuthConfig.getId(), newAuthConfig);
        organizationDomain.setConfigs(new ArrayList<>(authConfigMap.values()));

        return true;

    }

    // static inner class

    protected record FindByAuthUser(boolean userExist, User user) {
    }

    protected record VisitorBindAuthConnectionResult(@Nullable String orgId, String visitorId) {
    }
}
