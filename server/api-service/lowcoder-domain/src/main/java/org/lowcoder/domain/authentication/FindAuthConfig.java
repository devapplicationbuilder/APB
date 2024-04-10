package org.quickdev.domain.authentication;

import javax.annotation.Nullable;

import org.quickdev.domain.organization.model.Organization;
import org.quickdev.sdk.auth.AbstractAuthConfig;

public record FindAuthConfig(AbstractAuthConfig authConfig, @Nullable Organization organization) {
}