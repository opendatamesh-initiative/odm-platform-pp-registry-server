package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.client.notification.resources.dataProductVersion.EventContentDataProductVersionPublished;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;

class DataProductVersionApproverNotificationOutboundPortImpl implements DataProductVersionApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductVersionMapper dataProductVersionMapper;

    public DataProductVersionApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductVersionMapper dataProductVersionMapper) {
        this.notificationClient = notificationClient;
        this.dataProductVersionMapper = dataProductVersionMapper;
    }
    
    @Override
    public void emitDataProductVersionPublished(DataProductVersion dataProductVersion) {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT_VERSION);
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.setType(EventTypeRes.DATA_PRODUCT_VERSION_PUBLISHED);
        event.setEventTypeVersion(EventRes.EventTypeVersion.V1_0_0);
        EventContentDataProductVersionPublished eventContent = new EventContentDataProductVersionPublished(dataProductVersionMapper.toRes(dataProductVersion));
        event.setEventContent(eventContent);
        notificationClient.notifyEvent(event);
    }
}
