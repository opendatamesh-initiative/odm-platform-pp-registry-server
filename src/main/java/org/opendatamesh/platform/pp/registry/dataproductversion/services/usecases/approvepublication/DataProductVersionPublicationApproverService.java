package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove.EmittedEventDataProductVersionPublicationApprovedRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProductVersionPublicationApproverService {

    @Autowired
    private NotificationClient notificationClient;

    public void emitDataProductVersionPublicationApprovedEvent(DataProductVersionRes dataProductVersion) {
        EmittedEventDataProductVersionPublicationApprovedRes.DataProductVersion eventDataProductVersion = new EmittedEventDataProductVersionPublicationApprovedRes.DataProductVersion();
        eventDataProductVersion.setUuid(dataProductVersion.getUuid());
        eventDataProductVersion.setTag(dataProductVersion.getTag());
        
        EmittedEventDataProductVersionPublicationApprovedRes.DataProduct eventDataProduct = new EmittedEventDataProductVersionPublicationApprovedRes.DataProduct();
        if (dataProductVersion.getDataProduct() != null) {
            eventDataProduct.setUuid(dataProductVersion.getDataProduct().getUuid());
            eventDataProduct.setFqn(dataProductVersion.getDataProduct().getFqn());
        }
        eventDataProductVersion.setDataProduct(eventDataProduct);
        
        EmittedEventDataProductVersionPublicationApprovedRes event = new EmittedEventDataProductVersionPublicationApprovedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersion(eventDataProductVersion);
        notificationClient.notifyEvent(event);
    }
}

