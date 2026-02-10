package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "odm.product-plane.policy-service.active", havingValue = "true")
@ConditionalOnProperty(name = "odm.product-plane.policy-service.version", havingValue = "1")
public class PolicyServiceV1Configuration {
    private static final Logger logger = LoggerFactory.getLogger(PolicyServiceV1Configuration.class);

    @Value("${odm.product-plane.notification-service.active}")
    private boolean notificationServiceActive;
    @Value("${odm.descriptor.parser.version:1}")
    private String descriptorParserVersion;

    @Bean
    public NotificationEventHandler notificationEventHandlerDpInitializationRequested(
            NotificationClient notificationClient,
            PolicyClientV1 policyClient) {
        logger.info("Registering NotificationEventHandlerDpInitializationRequested");
        return new NotificationEventHandlerDpInitializationRequested(notificationClient, policyClient);
    }

    @Bean
    public NotificationEventHandler notificationEventHandlerDpvPublicationRequested(
            NotificationClient notificationClient,
            PolicyClientV1 policyClient) {
        logger.info("Registering NotificationEventHandlerDpvPublicationRequested (descriptor.parser.version={})", descriptorParserVersion);
        return new NotificationEventHandlerDpvPublicationRequested(notificationClient, policyClient, descriptorParserVersion);
    }
}
