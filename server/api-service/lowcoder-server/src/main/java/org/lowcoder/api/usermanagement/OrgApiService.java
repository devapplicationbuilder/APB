package org.quickdev.api.usermanagement;

import org.quickdev.api.authentication.dto.OrganizationDomainCheckResult;
import org.quickdev.api.config.ConfigView;
import org.quickdev.api.usermanagement.view.OrgMemberListView;
import org.quickdev.api.usermanagement.view.OrgView;
import org.quickdev.api.usermanagement.view.UpdateOrgRequest;
import org.quickdev.api.usermanagement.view.UpdateRoleRequest;
import org.quickdev.domain.organization.model.Organization;
import org.quickdev.domain.organization.model.Organization.OrganizationCommonSettings;
import org.quickdev.infra.annotation.NonEmptyMono;
import org.springframework.http.codec.multipart.Part;

import reactor.core.publisher.Mono;


public interface OrgApiService {

    Mono<Boolean> leaveOrganization(String orgId);

    @NonEmptyMono
    Mono<OrgMemberListView> getOrganizationMembers(String orgId, int page, int count);

    Mono<Boolean> updateRoleForMember(String orgId, UpdateRoleRequest updateRoleRequest);

    Mono<Boolean> switchCurrentOrganizationTo(String orgId);

    Mono<Boolean> deleteLogo(String orgId);

    Mono<Boolean> uploadLogo(String orgId, Mono<Part> fileMono);

    Mono<Boolean> removeUserFromOrg(String orgId, String userId);

    Mono<Boolean> removeOrg(String orgId);

    Mono<OrgView> create(Organization organization);

    Mono<Boolean> update(String orgId, UpdateOrgRequest updateOrgRequest);

    Mono<OrganizationDomainCheckResult> checkOrganizationDomain();

    Mono<OrganizationCommonSettings> getOrgCommonSettings(String orgId);

    Mono<Boolean> updateOrgCommonSettings(String orgId, String key, Object value);

    Mono<Boolean> tryAddUserToOrgAndSwitchOrg(String orgId, String userId);

    Mono<Void> checkMaxOrgMemberCount(String orgId);

    Mono<ConfigView> getOrganizationConfigs(String orgId);

    Mono<Long> getApiUsageCount(String orgId, Boolean lastMonthOnly);
}

