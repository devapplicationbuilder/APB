package org.quickdev.sdk.config;

import org.quickdev.sdk.config.dynamic.ConfigCenter;
import org.quickdev.sdk.config.dynamic.ConfigInstanceHelper;
import org.springframework.stereotype.Component;

@Component
public class CommonConfigHelper {

    private final CommonConfig commonConfig;
    private final ConfigInstanceHelper configInstanceHelper;

    public CommonConfigHelper(CommonConfig commonConfig, ConfigCenter configCenter) {
        this.commonConfig = commonConfig;
        this.configInstanceHelper = new ConfigInstanceHelper(configCenter.deployment());
    }

    public String getHost() {
        return configInstanceHelper.ofString("js-executor.host", commonConfig.getJsExecutor().getHost());
    }
}
