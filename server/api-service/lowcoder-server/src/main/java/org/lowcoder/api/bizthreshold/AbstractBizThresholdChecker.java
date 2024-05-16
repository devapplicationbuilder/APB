package org.quickdev.api.bizthreshold;

import static org.quickdev.sdk.util.ExceptionUtils.deferredError;
import static org.quickdev.sdk.util.ExceptionUtils.ofError;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quickdev.domain.application.model.ApplicationStatus;
import org.quickdev.domain.application.service.ApplicationService;
import org.quickdev.domain.group.model.GroupMember;
import org.quickdev.domain.group.service.GroupMemberService;
import org.quickdev.domain.group.service.GroupService;
import org.quickdev.domain.organization.model.OrgMember;
import org.quickdev.domain.organization.service.OrgMemberService;
import org.quickdev.infra.util.TupleUtils;
import org.quickdev.sdk.exception.BizError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public abstract class AbstractBizThresholdChecker {
    @Autowired
    private OrgMemberService orgMemberService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private ApplicationService applicationService;

    protected abstract int getMaxOrgPerUser();

    protected abstract int getMaxOrgMemberCount();

    protected abstract int getMaxOrgGroupCount();

    protected abstract int getMaxOrgAppCount();

    protected abstract Map<String, Integer> getUserOrgCountWhiteList();

    protected abstract Map<String, Integer> getOrgMemberCountWhiteList();

    protected abstract Map<String, Integer> getOrgAppCountWhiteList();

    protected abstract Mono<Integer> getMaxDeveloperCount();

    public Mono<Void> checkMaxOrgCount(String userId) {
        return orgMemberService.countAllActiveOrgs(userId)
                .filter(userOrgCount -> userOrgCountBelowThreshold(userId, userOrgCount))
                .switchIfEmpty(deferredError(BizError.EXCEED_MAX_USER_ORG_COUNT, "EXCEED_MAX_USER_ORG_COUNT"))
                .then();
    }

    private boolean userOrgCountBelowThreshold(String userId, long userOrgCount) {
        return userOrgCount < getMaxOrgPerUser();
    }

    public Mono<Void> checkMaxOrgMemberCount(String orgId) {
        return orgMemberService.getOrgMemberCount(orgId)
                .flatMap(orgMemberCount -> orgMemberCountBelowThreshold(orgId, orgMemberCount)
                        .flatMap(isBelowThreshold -> {
                            if (isBelowThreshold) {
                                return Mono.empty(); // Return empty Mono to indicate success
                            } else {
                                return deferredError(BizError.EXCEED_MAX_ORG_MEMBER_COUNT, "EXCEED_MAX_ORG_MEMBER_COUNT");
                            }
                        }))
                .then();
    }

    private Mono<Boolean> orgMemberCountBelowThreshold(String orgId, Long orgMemberCount) {
        return getItemFromLicense(orgId, "MAX_USERS")
                .map(maxUsers -> orgMemberCount < Long.parseLong(maxUsers))
                .defaultIfEmpty(false);
    }

    public Mono<Void> checkMaxGroupCount(OrgMember orgMemberMono) {
        return groupService.getOrgGroupCount(orgMemberMono.getOrgId())
                .flatMap(orgGroupCount -> orgGroupCountBelowThreshold(orgMemberMono.getOrgId(), orgGroupCount)
                        .flatMap(isBelowThreshold -> {
                            if (isBelowThreshold) {
                                return Mono.empty(); // Return empty Mono to indicate success
                            } else {
                                return deferredError(BizError.EXCEED_MAX_GROUP_COUNT, "EXCEED_MAX_GROUP_COUNT");
                            }
                        }))
                .then();
    }

    private Mono<Boolean> orgGroupCountBelowThreshold(String orgId, Long orgGroupCount) {
        return getItemFromLicense(orgId, "MAX_GROUPS")
                .map(maxGroups -> orgGroupCount < Long.parseLong(maxGroups) + 2)
                .defaultIfEmpty(false);
    }

    public Mono<Void> checkMaxOrgApplicationCount(OrgMember orgMember) {
        String orgId = orgMember.getOrgId();
        return applicationService.countByOrganizationId(orgId, ApplicationStatus.NORMAL)
                .flatMap(orgAppCount -> orgAppCountBelowThreshold(orgId, orgAppCount)
                        .flatMap(isBelowThreshold -> {
                            if (isBelowThreshold) {
                                return Mono.empty(); // Return empty Mono to indicate success
                            } else {
                                return deferredError(BizError.EXCEED_MAX_APP_COUNT, "EXCEED_MAX_APP_COUNT");
                            }
                        }))
                .then(); // Convert the result to Mono<Void>
    }

    public Mono<String> getItemFromLicense(String orgId, String type) {
        String authUrl = System.getenv("QUICKDEV_API_URL");
        String url = authUrl + "/api/License/" + orgId + "/GetLicense";

        WebClient webClient = WebClient.create();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(license -> {

                    String[] parts = license.split("#");

                    String maxApps = parts[0];
                    String maxGroups = parts[1];
                    String maxUsers = parts[2];
                    String maxDevs = parts[3];
                    String canPublish = parts[4];
                    String canView = parts[5];

                    if (type.equalsIgnoreCase("MAX_APPS")) {
                        return Mono.just(maxApps);
                    }
                    else if (type.equalsIgnoreCase("MAX_GROUPS")) {
                        return Mono.just(maxGroups);
                    }
                    else if (type.equalsIgnoreCase("MAX_USERS")) {
                        return Mono.just(maxUsers);
                    }
                    else if (type.equalsIgnoreCase("MAX_DEVELOPERS")) {
                        return Mono.just(maxDevs);
                    }
                    else if (type.equalsIgnoreCase("CAN_PUBLISH")) {
                        return Mono.just(canPublish);
                    }
                    else if (type.equalsIgnoreCase("CAN_VIEW")){
                        return Mono.just(canView);
                    }

                    return Mono.empty();
                });
    }

    private Mono<Boolean> orgAppCountBelowThreshold(String orgId, long orgAppCount) {
        return getItemFromLicense(orgId, "MAX_APPS")
                .map(maxApps -> orgAppCount < Long.parseLong(maxApps))
                .defaultIfEmpty(false); // Return false if getItemFromLicense returns empty
    }

    private Mono<Boolean> orgDeveloperCountBelowThreshold(String orgId, long orgDeveloperCount) {
        return getItemFromLicense(orgId, "MAX_DEVELOPERS")
                .map(maxDevelopers -> orgDeveloperCount < Long.parseLong(maxDevelopers) + 1)
                .defaultIfEmpty(false); // Return false if getItemFromLicense returns empty
    }

    public Mono<Void> checkMaxDeveloperCount(String orgId, String developGroupId, String userId) {
        return orgMemberService.getAllOrgAdmins(orgId)
                .zipWith(groupMemberService.getGroupMembers(developGroupId, 1, 9999))
                .zipWith(getMaxDeveloperCount(), TupleUtils::merge)
                .flatMap(tuple -> {
                    List<OrgMember> t1 = tuple.getT1();
                    List<GroupMember> t2 = tuple.getT2();
                    Integer t3 = tuple.getT3();
                    Set<String> developerIds = Stream.concat(t1.stream().map(OrgMember::getUserId), t2.stream().map(GroupMember::getUserId))
                            .collect(Collectors.toSet());
                    developerIds.add(userId);
                    //MAX_DEVELOPERS = 5

                    return orgDeveloperCountBelowThreshold(orgId, developerIds.size())
                            .flatMap(isBelowThreshold -> {
                                if (isBelowThreshold) {
                                    return Mono.empty(); // Return empty Mono to indicate success
                                } else {
                                    return deferredError(BizError.EXCEED_MAX_APP_COUNT, "EXCEED_MAX_DEVELOPER_COUNT");
                                }
                            })
                            .then();
                });
    }
}
