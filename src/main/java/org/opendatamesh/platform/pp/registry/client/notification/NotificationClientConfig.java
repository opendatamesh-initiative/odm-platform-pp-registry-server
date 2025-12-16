package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.utils.client.RestUtilsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public NotificationClient notificationClient(RestTemplateBuilder restTemplateBuilder) {
        if (!notificationServiceActive) {
            logger.warn("Notification service is not active. Events will not be sent.");
            return createDummyNotificationClient();
        }

        NotificationClient notificationClient = new NotificationClientImpl(baseUrl, observerName, observerDisplayName, notificationServiceBaseUrl, RestUtilsFactory.getRestUtils(restTemplateBuilder.build()));
        logger.info("Checking connection to Notification service at {}", notificationServiceBaseUrl);
        notificationClient.assertConnection();
        return notificationClient;

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
