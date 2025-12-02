package org.opendatamesh.platform.pp.registry.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtilsFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class NotificationClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(NotificationClientConfig.class);

    @Value("${server.baseUrl}")
    private String baseUrl;

    @Value("${server.observer.name}")
    private String observerName;

    @Value("${server.observer.displayName}")
    private String observerDisplayName;

    @Autowired
    private Environment environment;

    @Value("${odm.product-plane.notification-service.address}")
    private String notificationServiceBaseUrl;

    @Value("${odm.product-plane.notification-service.active}")
    private boolean notificationServiceActive;

    @Bean
    public NotificationClient notificationClient() {
        // Use @Value with Environment to get YAML lists
        List<String> eventTypes = getListProperty("server.observer.event-types");
        List<String> policyEventTypes = getListProperty("server.observer.policy-event-types");

        if (notificationServiceActive) {
            NotificationClient notificationClient = new NotificationClientImpl(baseUrl, observerName, observerDisplayName, notificationServiceBaseUrl, RestUtilsFactory.getRestUtils(new RestTemplate()));
            logger.info("Checking connection to Notification service at {}", notificationServiceBaseUrl);
            notificationClient.assertConnection();
            notificationClient.subscribeToEvents(eventTypes, policyEventTypes);
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
            public void subscribeToEvents(List<String> eventTypes, List<String> policyEventTypes) {
                logger.warn("Notification service is not active. Events not subscribed.");
            }

            @Override
            public void notifySuccess(Long notificationId) {
                logger.warn("Notification service is not active. Notification success not sent: {}", notificationId);
            }

            @Override
            public void notifyFailure(Long notificationId) {
                logger.warn("Notification service is not active. Notification failure not sent: {}", notificationId);
            }
        };
    }

    /**
     * Helper method to extract list properties from YAML using Environment.
     * This works with @Value by using Environment to access YAML lists.
     */
    private List<String> getListProperty(String propertyKey) {
        List<String> result = new ArrayList<>();
        int index = 0;
        while (true) {
            String value = environment.getProperty(propertyKey + "[" + index + "]");
            if (value == null) {
                break;
            }
            result.add(value);
            index++;
        }
        return result;
    }
}
