package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.EventDataProductDeletedRes;

class DataProductDeleterNotificationOutboundPortImpl implements DataProductDeleterNotificationOutboundPort {
    private final NotificationClient notificationClient;

    public DataProductDeleterNotificationOutboundPortImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public void emitDataProductDeleted(DataProduct dataProduct) {
        EventDataProductDeletedRes event = new EventDataProductDeletedRes();
        event.setResourceIdentifier(dataProduct.getUuid());
        event.getEventContent().setDataProductUuid(dataProduct.getUuid());
        event.getEventContent().setDataProductFqn(dataProduct.getFqn());
        notificationClient.notifyEvent(event);
    }
}

