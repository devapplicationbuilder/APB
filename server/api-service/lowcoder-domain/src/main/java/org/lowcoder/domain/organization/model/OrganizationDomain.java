package org.quickdev.domain.organization.model;

import java.util.ArrayList;
import java.util.List;

import org.quickdev.domain.mongodb.MongodbInterceptorContext;
import org.quickdev.sdk.auth.AbstractAuthConfig;
import org.quickdev.sdk.config.SerializeConfig.JsonViews;
import org.quickdev.sdk.util.JsonUtils;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;
import lombok.Setter;

public class OrganizationDomain implements EnterpriseConnectionConfig {

    @Getter
    @Setter
    private String domain;

    @Setter
    @Getter
    @Transient
    private List<AbstractAuthConfig> configs = new ArrayList<>();

    /**
     * Only used for mongodb (de)serialization
     */
    private List<Object> authConfigs = new ArrayList<>();

    void beforeMongodbWrite(MongodbInterceptorContext context) {
        this.configs.forEach(authConfig -> authConfig.doEncrypt(s -> context.encryptionService().encryptString(s)));
        authConfigs = JsonUtils.fromJsonSafely(JsonUtils.toJsonSafely(configs, JsonViews.Internal.class), new TypeReference<>() {
        }, new ArrayList<>());
    }

    void afterMongodbRead(MongodbInterceptorContext context) {
        this.configs = JsonUtils.fromJsonSafely(JsonUtils.toJson(authConfigs), new TypeReference<>() {
        }, new ArrayList<>());
        this.configs.forEach(authConfig -> authConfig.doDecrypt(s -> context.encryptionService().decryptString(s)));
    }
}
