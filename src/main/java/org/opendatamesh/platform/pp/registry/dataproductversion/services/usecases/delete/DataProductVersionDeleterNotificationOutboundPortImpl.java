package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.EventDataProductVersionDeletedRes;

class DataProductVersionDeleterNotificationOutboundPortImpl implements DataProductVersionDeleterNotificationOutboundPort {
    private final NotificationClient notificationClient;

    public DataProductVersionDeleterNotificationOutboundPortImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public void emitDataProductVersionDeleted(DataProductVersionShort dataProductVersion) {
        EventDataProductVersionDeletedRes event = new EventDataProductVersionDeletedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersionUuid(dataProductVersion.getUuid());
        event.getEventContent().setDataProductFqn(dataProductVersion.getDataProduct().getFqn());
        event.getEventContent().setDataProductVersionTag(dataProductVersion.getTag());
        notificationClient.notifyEvent(event);
    }
}

