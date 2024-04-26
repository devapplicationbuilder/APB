package org.quickdev.api.usermanagement;


import static org.quickdev.sdk.constants.Authentication.isAnonymousUser;
import static org.quickdev.sdk.exception.BizError.INVITED_USER_NOT_LOGIN;

import org.quickdev.api.framework.view.ResponseView;
import org.quickdev.api.home.SessionUserService;
import org.quickdev.api.usermanagement.view.InvitationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class InvitationController implements InvitationEndpoints
{

    @Autowired
    private InvitationApiService invitationApiService;

    @Autowired
    private SessionUserService sessionUserService;

    @Override
    public Mono<ResponseView<InvitationVO>> create(@RequestParam String orgId) {
        return invitationApiService.create(orgId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<InvitationVO>> get(@PathVariable String invitationId) {
        return invitationApiService.getInvitationView(invitationId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<?>> inviteUser(@PathVariable String invitationId) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> {
                            if (isAnonymousUser(visitorId)) {
                                return invitationApiService.getInvitationView(invitationId)
                                        .map(invitationVO -> ResponseView.error(INVITED_USER_NOT_LOGIN.getBizErrorCode(), "", invitationVO));
                            }
                            return invitationApiService.inviteUser(invitationId)
                                    .map(ResponseView::success);
                        }
                );
    }

}
