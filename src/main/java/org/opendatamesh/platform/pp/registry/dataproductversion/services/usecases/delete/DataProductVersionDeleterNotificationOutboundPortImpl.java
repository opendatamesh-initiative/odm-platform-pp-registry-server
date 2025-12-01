package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.client.notification.resources.dataProductVersion.EventContentDataProductVersionDeleted;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;

class DataProductVersionDeleterNotificationOutboundPortImpl implements DataProductVersionDeleterNotificationOutboundPort {
    private final NotificationClient notificationClient;

    public DataProductVersionDeleterNotificationOutboundPortImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public void emitDataProductVersionDeleted(DataProductVersionShort dataProductVersion) {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT_VERSION);
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.setType(EventTypeRes.DATA_PRODUCT_VERSION_DELETED);
        event.setEventTypeVersion(EventRes.EventTypeVersion.V1_0_0);
        EventContentDataProductVersionDeleted eventContent = new EventContentDataProductVersionDeleted(dataProductVersion.getUuid(), dataProductVersion.getDataProduct().getFqn(), dataProductVersion.getTag());
        event.setEventContent(eventContent);
        notificationClient.notifyEvent(event);
    }
}

