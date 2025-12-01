package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.dataProduct.EventContentDataProductDeleted;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

class DataProductDeleterNotificationOutboundPortImpl implements DataProductDeleterNotificationOutboundPort {
    private final NotificationClient notificationClient;

    public DataProductDeleterNotificationOutboundPortImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public void emitDataProductDeleted(DataProduct dataProduct) {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT);
        event.setResourceIdentifier(dataProduct.getUuid());
        event.setType(EventTypeRes.DATA_PRODUCT_DELETED);
        event.setEventTypeVersion(EventRes.EventTypeVersion.V1_0_0);
        EventContentDataProductDeleted eventContent = new EventContentDataProductDeleted(dataProduct.getUuid(), dataProduct.getFqn());
        event.setEventContent(eventContent);
        notificationClient.notifyEvent(event);
    }
}

