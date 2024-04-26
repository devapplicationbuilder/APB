package org.quickdev.sdk.plugin.restapi.auth;

import javax.annotation.Nullable;

import org.quickdev.sdk.models.Encrypt;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true, defaultImpl = DefaultAuthConfig.class)
@JsonSubTypes({
        @Type(value = BasicAuthConfig.class, name = "DIGEST_AUTH"),
        @Type(value = BasicAuthConfig.class, name = "BASIC_AUTH"),
        @Type(value = NoneAuthConfig.class, name = "NO_AUTH"),
        @Type(value = OAuthInheritAuthConfig.class, name = "OAUTH2_INHERIT_FROM_LOGIN")
})
public abstract class AuthConfig implements Encrypt {

    protected final RestApiAuthType type;

    protected AuthConfig(RestApiAuthType type) {
        this.type = type;
    }

    public AuthConfig mergeWithUpdatedConfig(@Nullable AuthConfig updatedConfig) {
        return updatedConfig;
    }
}
