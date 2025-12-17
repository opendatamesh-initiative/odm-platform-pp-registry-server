package org.opendatamesh.platform.pp.registry.observer;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.exceptions.RegistryApiException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObserverService {
    private static final Logger logger = LoggerFactory.getLogger(ObserverService.class);

    @Autowired
    private List<NotificationEventHandler> notificationEventHandlers;

    @Autowired
    private NotificationClient notificationClient;

    @Async
    public void processNotification(NotificationDispatchRes notification) {
        Long notificationId = notification.getSequenceId();
        try {
           handleEvent(notification.getEvent());
            notificationClient.processingSuccess(notificationId);
        } catch (RegistryApiException e) {
            logger.warn("Failed to dispatch notification: {}", e.getMessage(), e);
            notificationClient.processingFailure(notificationId);
        } catch (Exception e) {
            logger.error("Failed to dispatch notification: {}", e.getMessage(), e);
            notificationClient.processingFailure(notificationId);
        }
    }

    private void handleEvent(NotificationDispatchEventRes event) {
        EventTypeRes eventType;
        try {
            eventType = EventTypeRes.fromString(event.getType());
        } catch (IllegalArgumentException e) {
            logger.info("Unsupported event type: {}. No handler will be used.", event.getType());
            return;
        }
        
        Optional<NotificationEventHandler> dispatcher = notificationEventHandlers.stream()
                .filter(d -> d.supportsEventType(eventType))
                .findFirst();

        if (!dispatcher.isPresent()) {
            logger.info("No handler found for event type: {}", eventType);
            return;
        }

        dispatcher.get().handleEvent(event);
        logger.info("Event {} handled successfully", eventType);
    }
}
