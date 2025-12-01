package org.opendatamesh.platform.pp.registry.utils.usecases;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;

public interface NotificationEventDispatcher {
    boolean supportsNotificationEventType(EventTypeRes eventType);
    void dispatchNotificationToUseCase(NotificationDispatchRes notification);
}
