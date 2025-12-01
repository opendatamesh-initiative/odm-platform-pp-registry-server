package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.dataProduct.EventContentDataProductInitialized;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;

class DataProductApproverNotificationOutboundPortImpl implements DataProductApproverNotificationOutboundPort {
    private final NotificationClient notificationClient;
    private final DataProductMapper dataProductMapper;

    public DataProductApproverNotificationOutboundPortImpl(NotificationClient notificationClient, DataProductMapper dataProductMapper) {
        this.notificationClient = notificationClient;
        this.dataProductMapper = dataProductMapper;
    }

    @Override
    public void emitDataProductInitialized(DataProduct dataProduct) {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT);
        event.setResourceIdentifier(dataProduct.getUuid());
        event.setType(EventTypeRes.DATA_PRODUCT_INITIALIZED);
        event.setEventTypeVersion(EventRes.EventTypeVersion.V1_0_0);
        EventContentDataProductInitialized eventContent = new EventContentDataProductInitialized(dataProductMapper.toRes(dataProduct));
        event.setEventContent(eventContent);
        notificationClient.notifyEvent(event);
    }
}
