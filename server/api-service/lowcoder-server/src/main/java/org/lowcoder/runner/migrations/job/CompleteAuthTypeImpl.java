package org.quickdev.runner.migrations.job;

import org.apache.commons.collections4.CollectionUtils;
import org.quickdev.domain.organization.model.Organization;
import org.quickdev.domain.organization.service.OrganizationService;
import org.quickdev.sdk.auth.AbstractAuthConfig;
import org.quickdev.sdk.auth.EmailAuthConfig;
import org.quickdev.sdk.auth.constants.AuthTypeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class CompleteAuthTypeImpl implements CompleteAuthType {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;
    @Autowired
    private OrganizationService organizationService;

    @Override
    public void complete() {
        Criteria criteria = Criteria.where("organizationDomain").ne(null);
        reactiveMongoTemplate.find(new Query(criteria), Organization.class)
                .filter(organization -> CollectionUtils.isNotEmpty(organization.getAuthConfigs()))
                .doOnNext(organization -> organization.getAuthConfigs().forEach(this::completeAuthType))
                .flatMap(organization -> organizationService.update(organization.getId(), organization))
                .blockLast();
    }

    protected void completeAuthType(AbstractAuthConfig abstractAuthConfig) {
        if (abstractAuthConfig instanceof EmailAuthConfig emailAuthConfig) {
            emailAuthConfig.setAuthType(AuthTypeConstants.FORM);
        }
    }
}
