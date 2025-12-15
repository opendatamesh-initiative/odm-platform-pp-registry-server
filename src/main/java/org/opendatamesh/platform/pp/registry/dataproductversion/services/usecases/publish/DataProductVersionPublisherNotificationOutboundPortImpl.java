package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionPublicationRequestedRes;

class DataProductVersionPublisherNotificationOutboundPortImpl implements DataProductVersionPublisherNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductVersionMapper dataProductVersionMapper;

    public DataProductVersionPublisherNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductVersionMapper dataProductVersionMapper) {
        this.notificationClient = notificationClient;
        this.dataProductVersionMapper = dataProductVersionMapper;
    }
    @Override
    public void emitDataProductVersionPublicationRequested(DataProductVersion dataProductVersion, DataProductVersion previousDataProductVersion) {
        EmittedEventDataProductVersionPublicationRequestedRes event = new EmittedEventDataProductVersionPublicationRequestedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersion(dataProductVersionMapper.toRes(dataProductVersion));
        if (previousDataProductVersion != null) {
            event.getEventContent().setPreviousDataProductVersion(dataProductVersionMapper.toRes(previousDataProductVersion));
        }
        notificationClient.notifyEvent(event);
    }
}
