package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionPublicationApproverFactory {

    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private DataProductVersionMapper dataProductVersionMapper;

    public UseCase buildDataProductVersionPublicationApprover(DataProductVersionPublicationApproverCommand command, DataProductVersionPublicationApproverPresenter presenter) {
        DataProductVersionPublicationApproverNotificationOutboundPort notificationPort = new DataProductVersionPublicationApproverNotificationOutboundPortImpl(notificationClient, dataProductVersionMapper);
        return new DataProductVersionPublicationApprover(command, presenter, notificationPort);
    }
}

