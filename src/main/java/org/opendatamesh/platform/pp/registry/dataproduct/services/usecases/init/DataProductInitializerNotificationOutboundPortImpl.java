package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.EventDataProductInitializationRequestedRes;

class DataProductInitializerNotificationOutboundPortImpl implements DataProductInitializerNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductMapper dataProductMapper;

    public DataProductInitializerNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductMapper dataProductMapper) {
        this.notificationClient = notificationClient;
        this.dataProductMapper = dataProductMapper;
    }

    @Override
    public void emitDataProductInitializationRequested(DataProduct dataProduct) {
        EventDataProductInitializationRequestedRes event = new EventDataProductInitializationRequestedRes();
        event.setResourceIdentifier(dataProduct.getUuid());
        event.getEventContent().setDataProduct(dataProductMapper.toRes(dataProduct));
        notificationClient.notifyEvent(event);
    }
}
