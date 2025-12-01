package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.client.notification.resources.dataProductVersion.EventContentDataProductVersionPublicationRequested;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;

class DataProductVersionPublisherNotificationOutboundPortImpl implements DataProductVersionPublisherNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductVersionMapper dataProductVersionMapper;

    public DataProductVersionPublisherNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductVersionMapper dataProductVersionMapper) {
        this.notificationClient = notificationClient;
        this.dataProductVersionMapper = dataProductVersionMapper;
    }
    @Override
    public void emitDataProductVersionPublicationRequested(DataProductVersion dataProductVersion) {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT_VERSION);
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.setType(EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED);
        event.setEventTypeVersion(EventRes.EventTypeVersion.V1_0_0);
        EventContentDataProductVersionPublicationRequested eventContent = new EventContentDataProductVersionPublicationRequested(dataProductVersionMapper.toRes(dataProductVersion));
        event.setEventContent(eventContent);
        notificationClient.notifyEvent(event);
    }
}
