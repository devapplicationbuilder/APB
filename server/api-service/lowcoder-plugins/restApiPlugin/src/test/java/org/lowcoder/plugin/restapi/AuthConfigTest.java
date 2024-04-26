package org.quickdev.plugin.restapi;

import static org.quickdev.sdk.plugin.restapi.auth.RestApiAuthType.BASIC_AUTH;
import static org.quickdev.sdk.plugin.restapi.auth.RestApiAuthType.BEARER_TOKEN_AUTH;
import static org.quickdev.sdk.plugin.restapi.auth.RestApiAuthType.DIGEST_AUTH;
import static org.quickdev.sdk.plugin.restapi.auth.RestApiAuthType.NO_AUTH;

import org.junit.Assert;
import org.junit.Test;
import org.quickdev.sdk.plugin.restapi.RestApiDatasourceConfig;
import org.quickdev.sdk.plugin.restapi.auth.BasicAuthConfig;
import org.quickdev.sdk.plugin.restapi.auth.DefaultAuthConfig;
import org.quickdev.sdk.plugin.restapi.auth.NoneAuthConfig;
import org.quickdev.sdk.util.JsonUtils;

public class AuthConfigTest {

    @Test
    public void testRestApiDatasourceConfigDeserialize4BasicAuthType() {
        String json = """
                {
                  "headers": [
                    {
                      "key": "Content-Type",
                      "value": "application/json"
                    }
                  ],
                  "authConfig": {
                    "type": "BASIC_AUTH",
                    "username": "jack",
                    "password": "123456"
                  },
                  "url": "http://localhost:8070"
                }
                """;
        RestApiDatasourceConfig restApiDatasourceConfig = JsonUtils.fromJson(json, RestApiDatasourceConfig.class);
        Assert.assertNotNull(restApiDatasourceConfig);
        BasicAuthConfig basicAuthConfig = (BasicAuthConfig) restApiDatasourceConfig.getAuthConfig();
        Assert.assertEquals(BASIC_AUTH, basicAuthConfig.getType());
        Assert.assertEquals("jack", basicAuthConfig.getUsername());
        Assert.assertEquals("123456", basicAuthConfig.getPassword());
    }

    @Test
    public void testRestApiDatasourceConfigDeserialize4DigestAuthType() {
        String json = """
                {
                  "headers": [
                    {
                      "key": "Content-Type",
                      "value": "application/json"
                    }
                  ],
                  "authConfig": {
                    "type": "DIGEST_AUTH",
                    "username": "jack",
                    "password": "123456"
                  },
                  "url": "http://localhost:8070"
                }
                """;
        RestApiDatasourceConfig restApiDatasourceConfig = JsonUtils.fromJson(json, RestApiDatasourceConfig.class);
        Assert.assertNotNull(restApiDatasourceConfig);
        BasicAuthConfig basicAuthConfig = (BasicAuthConfig) restApiDatasourceConfig.getAuthConfig();
        Assert.assertEquals(DIGEST_AUTH, basicAuthConfig.getType());
        Assert.assertEquals("jack", basicAuthConfig.getUsername());
        Assert.assertEquals("123456", basicAuthConfig.getPassword());
    }

    @Test
    public void testRestApiDatasourceConfigDeserialize4NoAuthType() {
        String json = """
                {
                  "headers": [
                    {
                      "key": "Content-Type",
                      "value": "application/json"
                    }
                  ],
                  "authConfig": {
                    "type": "NO_AUTH",
                    "username": "jack",
                    "password": "123456"
                  },
                  "url": "http://localhost:8070"
                }
                """;
        RestApiDatasourceConfig restApiDatasourceConfig = JsonUtils.fromJson(json, RestApiDatasourceConfig.class);
        Assert.assertNotNull(restApiDatasourceConfig);
        NoneAuthConfig noneAuthConfig = (NoneAuthConfig) restApiDatasourceConfig.getAuthConfig();
        Assert.assertEquals(NO_AUTH, noneAuthConfig.getType());
    }

    @Test
    public void testRestApiDatasourceConfigDeserialize4DefaultAuthType() {
        String json = """
                {
                  "headers": [
                    {
                      "key": "Content-Type",
                      "value": "application/json"
                    }
                  ],
                  "authConfig": {
                    "type": "BEARER_TOKEN_AUTH",
                    "username": "jack",
                    "password": "123456"
                  },
                  "url": "http://localhost:8070"
                }
                """;
        RestApiDatasourceConfig restApiDatasourceConfig = JsonUtils.fromJson(json, RestApiDatasourceConfig.class);
        Assert.assertNotNull(restApiDatasourceConfig);
        DefaultAuthConfig defaultAuthConfig = (DefaultAuthConfig) restApiDatasourceConfig.getAuthConfig();
        Assert.assertEquals(BEARER_TOKEN_AUTH, defaultAuthConfig.getType());
    }
}