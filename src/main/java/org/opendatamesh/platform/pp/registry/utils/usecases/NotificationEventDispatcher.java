package org.opendatamesh.platform.pp.registry.utils.usecases;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;

public interface NotificationEventDispatcher {
    boolean supportsEventType(EventTypeRes eventType);
    void dispatchEventToUseCase(NotificationDispatchEventRes event);
}
