package org.quickdev.plugin;

import org.quickdev.sdk.query.QueryExecutionContext;
import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LowcoderApiQueryExecutionContext extends QueryExecutionContext {

    private int port;
    private String actionType;
    private String visitorId;
    private String applicationOrgId;
    private MultiValueMap<String, HttpCookie> requestCookies;
}
