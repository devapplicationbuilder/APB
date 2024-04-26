package org.quickdev.api.usermanagement;

import java.util.List;

import javax.validation.Valid;

import org.quickdev.api.authentication.dto.OrganizationDomainCheckResult;
import org.quickdev.api.framework.view.ResponseView;
import org.quickdev.api.usermanagement.view.OrgMemberListView;
import org.quickdev.api.usermanagement.view.OrgView;
import org.quickdev.api.usermanagement.view.UpdateOrgRequest;
import org.quickdev.api.usermanagement.view.UpdateRoleRequest;
import org.quickdev.api.util.BusinessEventPublisher;
import org.quickdev.domain.organization.model.Organization;
import org.quickdev.domain.organization.model.Organization.OrganizationCommonSettings;
import org.quickdev.domain.plugin.DatasourceMetaInfo;
import org.quickdev.domain.plugin.service.DatasourceMetaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class OrganizationController implements OrganizationEndpoints
{

    @Autowired
    private OrgApiService orgApiService;
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    @Override
    public Mono<ResponseView<OrgView>> create(@Valid @RequestBody Organization organization) {
        return orgApiService.create(organization)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> update(@PathVariable String orgId,
            @Valid @RequestBody UpdateOrgRequest updateOrgRequest) {
        return orgApiService.update(orgId, updateOrgRequest)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> uploadLogo(@PathVariable String orgId,
            @RequestPart("file") Mono<Part> fileMono) {
        return orgApiService.uploadLogo(orgId, fileMono)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> deleteLogo(@PathVariable String orgId) {
        return orgApiService.deleteLogo(orgId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<OrgMemberListView>> getOrgMembers(@PathVariable String orgId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "count", required = false, defaultValue = "1000") int count) {
        return orgApiService.getOrganizationMembers(orgId, page, count)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> updateRoleForMember(@RequestBody UpdateRoleRequest updateRoleRequest,
            @PathVariable String orgId) {
        return orgApiService.updateRoleForMember(orgId, updateRoleRequest)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<?>> setCurrentOrganization(@PathVariable String orgId, ServerWebExchange serverWebExchange) {
        return businessEventPublisher.publishUserLogoutEvent()
                .then(orgApiService.switchCurrentOrganizationTo(orgId))
                .delayUntil(result -> businessEventPublisher.publishUserLoginEvent(null))
                .flatMap(result -> orgApiService.checkOrganizationDomain()
                        .flatMap(OrganizationDomainCheckResult::buildOrganizationDomainCheckView)
                        .defaultIfEmpty(ResponseView.success(result)));
    }

    @Override
    public Mono<ResponseView<Boolean>> removeOrg(@PathVariable String orgId) {
        return orgApiService.removeOrg(orgId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> leaveOrganization(@PathVariable String orgId) {
        return orgApiService.leaveOrganization(orgId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> removeUserFromOrg(@PathVariable String orgId,
            @RequestParam String userId) {
        return orgApiService.removeUserFromOrg(orgId, userId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<List<DatasourceMetaInfo>>> getSupportedDatasourceTypes(@PathVariable String orgId) {
        return datasourceMetaInfoService.getAllSupportedDatasourceMetaInfos()
                .collectList()
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<OrganizationCommonSettings>> getOrgCommonSettings(@PathVariable String orgId) {
        return orgApiService.getOrgCommonSettings(orgId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> updateOrgCommonSettings(@PathVariable String orgId, @RequestBody UpdateOrgCommonSettingsRequest request) {
        return orgApiService.updateOrgCommonSettings(orgId, request.key(), request.value())
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Long>> getOrgApiUsageCount(String orgId, Boolean lastMonthOnly) {
        return orgApiService.getApiUsageCount(orgId, lastMonthOnly)
                .map(ResponseView::success);
    }

}