package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted.EmittedEventDataProductVersionInitializationApprovedRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProductVersionPublicationApproverService {

    @Autowired
    private NotificationClient notificationClient;

    public void emitDataProductVersionPublicationApprovedEvent(DataProductVersionRes dataProductVersion) {
        EmittedEventDataProductVersionInitializationApprovedRes event = new EmittedEventDataProductVersionInitializationApprovedRes();
        event.setResourceIdentifier(dataProductVersion.getUuid());
        event.getEventContent().setDataProductVersion(dataProductVersion);
        notificationClient.notifyEvent(event);
    }
}

