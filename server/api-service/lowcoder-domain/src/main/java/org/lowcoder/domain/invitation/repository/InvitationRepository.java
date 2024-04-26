package org.quickdev.domain.invitation.repository;

import org.quickdev.domain.invitation.model.Invitation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InvitationRepository extends ReactiveMongoRepository<Invitation, String>, CustomInvitationRepository {

}
