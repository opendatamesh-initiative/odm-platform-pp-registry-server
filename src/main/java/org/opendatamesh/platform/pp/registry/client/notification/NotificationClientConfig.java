package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.utils.client.RestUtilsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class NotificationClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(NotificationClientConfig.class);

    @Value("${server.baseUrl}")
    private String baseUrl;

    @Value("${registry.observer.name:registry2.0}")
    private String observerName;

    @Value("${registry.observer.displayName:Registry service 2.0}")
    private String observerDisplayName;

    @Value("${odm.product-plane.notification-service.address}")
    private String notificationServiceBaseUrl;

    @Value("${odm.product-plane.notification-service.active}")
    private boolean notificationServiceActive;

    @Value("${odm.product-plane.policy-service.active}")
    private boolean policyServiceActive;

    @Bean
    public NotificationClient notificationClient() {
        // Hardcoded event types that the Registry subscribes to
        List<String> eventTypes = List.of(
            "DATA_PRODUCT_INITIALIZATION_APPROVED",
            "DATA_PRODUCT_INITIALIZATION_REJECTED",
            "DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED",
            "DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED"
        );
        
        // Policy-related event types (used when Policy service is unavailable)
        List<String> policyEventTypes = List.of(
            "DATA_PRODUCT_INITIALIZATION_REQUESTED",
            "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
        );

        if (notificationServiceActive) {
            NotificationClient notificationClient = new NotificationClientImpl(baseUrl, observerName, observerDisplayName, notificationServiceBaseUrl, RestUtilsFactory.getRestUtils(new RestTemplate()));
            logger.info("Checking connection to Notification service at {}", notificationServiceBaseUrl);
            notificationClient.assertConnection();

            List<String> eventsToSubscribe = new ArrayList<>();
            eventsToSubscribe.addAll(eventTypes);
            if (!policyServiceActive) {
                logger.info("Policy service is not active. Adding policy event types to subscriptions for auto-approval workflows.");
                eventsToSubscribe.addAll(policyEventTypes);
            }
            notificationClient.subscribeToEvents(eventsToSubscribe);

            return notificationClient;
        }
        
        // Notification service is not active, return a dummy implementation that does nothing
        logger.warn("Notification service is not active. Events will not be sent.");
        return createDummyNotificationClient();
    }

    private NotificationClient createDummyNotificationClient() {
        return new NotificationClient() {
            @Override
            public void assertConnection() {
                logger.warn("Notification service is not active. Connection not checked.");
            }   

            @Override
            public void notifyEvent(Object event) {
                logger.warn("Notification service is not active. Event not sent: {}", event);
            }

            @Override
            public void subscribeToEvents(List<String> eventTypes) {
                logger.warn("Notification service is not active. Events not subscribed.");
            }

            @Override
            public void processingSuccess(Long notificationId) {
                logger.warn("Notification service is not active. Notification success not sent: {}", notificationId);
            }

            @Override
            public void processingFailure(Long notificationId) {
                logger.warn("Notification service is not active. Notification failure not sent: {}", notificationId);
            }
        };
    }

}
