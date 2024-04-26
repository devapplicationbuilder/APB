package org.quickdev.api.common;

import org.quickdev.api.home.SessionUserServiceImpl;
import org.quickdev.domain.organization.model.OrgMember;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Primary
@Service
public class SessionUserServiceImplTest extends SessionUserServiceImpl {

    @Override
    public Mono<OrgMember> getVisitorOrgMemberCache() {
        return super.getVisitorOrgMember();
    }
}
