package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.EventDataProductVersionPublishedRes;

class DataProductVersionApproverNotificationOutboundPortImpl implements DataProductVersionApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductVersionMapper dataProductVersionMapper;

    public DataProductVersionApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductVersionMapper dataProductVersionMapper) {
        this.notificationClient = notificationClient;
        this.dataProductVersionMapper = dataProductVersionMapper;
    }
    
    @Override
    public void emitDataProductVersionPublished(DataProductVersion dataProductVersion) {
        EventDataProductVersionPublishedRes event = new EventDataProductVersionPublishedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersion(dataProductVersionMapper.toRes(dataProductVersion));
        notificationClient.notifyEvent(event);
    }
}
