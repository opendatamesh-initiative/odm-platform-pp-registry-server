package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove.EmittedEventDataProductInitializationApprovedRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProductInitializationApproverService {

    @Autowired
    private NotificationClient notificationClient;

    public void emitDataProductInitializationApprovedEvent(DataProductRes dataProduct) {
        EmittedEventDataProductInitializationApprovedRes.DataProduct eventDataProduct = new EmittedEventDataProductInitializationApprovedRes.DataProduct();
        eventDataProduct.setUuid(dataProduct.getUuid());
        eventDataProduct.setFqn(dataProduct.getFqn());
        
        EmittedEventDataProductInitializationApprovedRes eventToEmit = new EmittedEventDataProductInitializationApprovedRes();
        eventToEmit.setResourceIdentifier(dataProduct.getUuid());
        eventToEmit.getEventContent().setDataProduct(eventDataProduct);
        notificationClient.notifyEvent(eventToEmit);
    }
}

