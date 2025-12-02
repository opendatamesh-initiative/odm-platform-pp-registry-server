package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.EventDataProductInitializedRes;

class DataProductApproverNotificationOutboundPortImpl implements DataProductApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductMapper dataProductMapper;

    public DataProductApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductMapper dataProductMapper) {
        this.notificationClient = notificationClient;
        this.dataProductMapper = dataProductMapper;
    }

    @Override
    public void emitDataProductInitialized(DataProduct dataProduct) {
        EventDataProductInitializedRes event = new EventDataProductInitializedRes();
        event.setResourceIdentifier(dataProduct.getUuid());
        event.getEventContent().setDataProduct(dataProductMapper.toRes(dataProduct));
        notificationClient.notifyEvent(event);
    }
}
