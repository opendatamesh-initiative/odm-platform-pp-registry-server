package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionInitializationApprovedRes;

class DataProductVersionPublicationApproverNotificationOutboundPortImpl implements DataProductVersionPublicationApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductVersionMapper dataProductVersionMapper;

    public DataProductVersionPublicationApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductVersionMapper dataProductVersionMapper) {
        this.notificationClient = notificationClient;
        this.dataProductVersionMapper = dataProductVersionMapper;
    }

    @Override
    public void emitDataProductVersionInitializationApproved(DataProductVersion dataProductVersion) {
        EmittedEventDataProductVersionInitializationApprovedRes event = new EmittedEventDataProductVersionInitializationApprovedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersion(dataProductVersionMapper.toRes(dataProductVersion));
        notificationClient.notifyEvent(event);
    }
}

