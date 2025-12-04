package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.emitted.EmittedEventDataProductInitializationApprovedRes;

class DataProductInitializationApproverNotificationOutboundPortImpl implements DataProductInitializationApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductMapper dataProductMapper;

    public DataProductInitializationApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductMapper dataProductMapper) {
        this.notificationClient = notificationClient;
        this.dataProductMapper = dataProductMapper;
    }

    @Override
    public void emitDataProductInitializationApproved(DataProduct dataProduct) {
        EmittedEventDataProductInitializationApprovedRes event = new EmittedEventDataProductInitializationApprovedRes();
        event.setResourceIdentifier(dataProduct.getUuid());
        event.getEventContent().setDataProduct(dataProductMapper.toRes(dataProduct));
        notificationClient.notifyEvent(event);
    }
}

