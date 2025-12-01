package org.opendatamesh.platform.pp.registry.observer;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ObserverService {
    @Autowired
    private List<NotificationEventDispatcher> notificationEventDispatchers;

    public void dispatchNotification(NotificationDispatchRes notification) {
        // Dispatch the event to the appropriate use case
        EventTypeRes eventType = EventTypeRes.fromString(notification.getEvent().getType());
        notificationEventDispatchers.stream()
                .filter(dispatcher -> dispatcher.supportsNotificationEventType(eventType))
                .findFirst().ifPresent(dispatcher -> dispatcher.dispatchNotificationToUseCase(notification));
    }
}
