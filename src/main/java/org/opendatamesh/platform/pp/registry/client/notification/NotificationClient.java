package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;

public interface NotificationClient {
    void assertConnection();
    void subscribeToEvents();
    void notifyEvent(EventRes event);
}
