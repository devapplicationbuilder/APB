package org.quickdev.api.authentication.service.factory;

import java.util.Set;

import org.quickdev.api.authentication.dto.AuthConfigRequest;
import org.quickdev.sdk.auth.AbstractAuthConfig;

public interface AuthConfigFactory {

    AbstractAuthConfig build(AuthConfigRequest authConfigRequest, boolean enable);

    Set<String> supportAuthTypes();
}
