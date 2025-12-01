package org.opendatamesh.platform.pp.registry.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtilsFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NotificationClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(NotificationClientConfig.class);

    @Value("${server.baseUrl}")
    private String baseUrl;

    @Value("${server.observer.name}")
    private String observerName;

    @Value("${server.observer.displayName}")
    private String observerDisplayName;

    @Value("${odm.product-plane.notification-service.address}")
    private String notificationServiceBaseUrl;

    @Value("${odm.product-plane.notification-service.active}")
    private boolean notificationServiceActive;

    @Bean
    public NotificationClient notificationClient() {
        if (notificationServiceActive) {
            NotificationClient notificationClient = new NotificationClientImpl(baseUrl, observerName, observerDisplayName, notificationServiceBaseUrl, RestUtilsFactory.getRestUtils(new RestTemplate()));
            logger.info("Checking connection to Notification service at {}", notificationServiceBaseUrl);
            notificationClient.assertConnection();
            notificationClient.subscribeToEvents();
            return notificationClient;
        }
        
        // Notification service is not active, return a dummy implementation that does nothing
        logger.warn("Notification service is not active. Events will not be sent.");
        return new NotificationClient() {
            @Override
            public void assertConnection() {
                logger.warn("Notification service is not active. Connection not checked.");
            }   

            @Override
            public void notifyEvent(EventRes event) {
                logger.warn("Notification service is not active. Event not sent: {}", event);
            }

            @Override
            public void subscribeToEvents() {
                logger.warn("Notification service is not active. Events not subscribed.");
            }
        };
    }
}
