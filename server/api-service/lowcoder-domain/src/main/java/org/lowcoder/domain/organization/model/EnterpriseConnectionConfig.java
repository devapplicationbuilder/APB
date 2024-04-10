package org.quickdev.domain.organization.model;

import java.util.List;

import org.quickdev.sdk.auth.AbstractAuthConfig;

public interface EnterpriseConnectionConfig {

    List<AbstractAuthConfig> getConfigs();
}
