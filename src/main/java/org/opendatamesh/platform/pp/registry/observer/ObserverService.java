package org.opendatamesh.platform.pp.registry.observer;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
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
    private List<NotificationEventDispatcher> notificationEventDispatchers;

    @Autowired
    private NotificationClient notificationClient;

    @Async
    public void dispatchNotification(NotificationDispatchRes notification) {
        // Dispatch the event to the appropriate use case
        Long notificationId = notification.getSequenceId();
        try {
            EventTypeRes eventType = EventTypeRes.fromString(notification.getEvent().getType());
            Optional<NotificationEventDispatcher> dispatcher = notificationEventDispatchers.stream()
                    .filter(d -> d.supportsNotificationEventType(eventType))
                    .findFirst();
            if (dispatcher.isPresent()) {
                dispatcher.get().dispatchNotificationToUseCase(notification); 
                notificationClient.notifySuccess(notificationId);
                logger.info("Notification for event type {} dispatched successfully", eventType);
            } else {
                logger.warn("No dispatcher found for event type: {}", eventType);
                notificationClient.notifyFailure(notificationId);
            }
        } catch (Exception e) {
            logger.error("Failed to dispatch notification: {}", e.getMessage(), e);
            notificationClient.notifyFailure(notificationId);
        }
    }
}
