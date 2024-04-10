package org.quickdev.api.authentication.dto;

import org.quickdev.domain.user.model.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginEndMessage {
    private User user;
    private boolean signUp;
    private String signUpSource;
    private String requestDomain;
    private String thirdPartyLoginOrgId;
}
