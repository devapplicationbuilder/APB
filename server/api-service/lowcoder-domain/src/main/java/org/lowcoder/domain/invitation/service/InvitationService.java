package org.quickdev.domain.invitation.service;


import javax.annotation.Nonnull;

import org.quickdev.domain.invitation.model.Invitation;
import org.quickdev.domain.invitation.repository.InvitationRepository;
import org.quickdev.domain.organization.model.MemberRole;
import org.quickdev.domain.organization.service.OrgMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Lazy
@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private OrgMemberService orgMemberService;

    @Autowired
    private InvitationRepository repository;

    public Mono<Invitation> create(Invitation invitation) {
        return repository.save(invitation);
    }

    public Mono<Invitation> getById(@Nonnull String invitationId) {
        return invitationRepository.findById(invitationId);
    }

    public Mono<Boolean> inviteToOrg(String userId, String orgId) {
        return orgMemberService.addMember(orgId, userId, MemberRole.MEMBER);
    }

}
