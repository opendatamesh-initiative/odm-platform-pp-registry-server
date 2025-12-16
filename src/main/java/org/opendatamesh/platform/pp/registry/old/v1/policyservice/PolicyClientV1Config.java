package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtilsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "odm.product-plane.policy-service.active", havingValue = "true")
@ConditionalOnProperty(name = "odm.product-plane.policy-service.version", havingValue = "1")
public class PolicyClientV1Config {
    private static final Logger logger = LoggerFactory.getLogger(PolicyClientV1Config.class);

    @Value("${odm.product-plane.policy-service.address}")
    private String policyServiceBaseUrl;

    @Bean
    public PolicyClientV1 policyClientV1(RestTemplateBuilder restTemplateBuilder) {
        RestUtils restUtils = RestUtilsFactory.getRestUtils(restTemplateBuilder.build());
        logger.info("Creating PolicyClientV1 with base URL: {}", policyServiceBaseUrl);
        return new PolicyClientV1Impl(restUtils, policyServiceBaseUrl);
    }
}
