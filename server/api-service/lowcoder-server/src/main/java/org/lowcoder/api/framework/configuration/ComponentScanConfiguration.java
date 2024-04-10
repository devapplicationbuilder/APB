package org.quickdev.api.framework.configuration;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "org.quickdev")
@Configuration
@ConditionalOnMissingClass("org.quickdev.api.framework.configuration.ComponentScanConfigurationEEVersion")
public class ComponentScanConfiguration {
}
