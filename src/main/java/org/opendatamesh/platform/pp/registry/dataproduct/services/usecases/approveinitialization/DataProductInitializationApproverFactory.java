package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductInitializationApproverFactory {

    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private DataProductMapper dataProductMapper;

    public UseCase buildDataProductInitializationApprover(DataProductInitializationApproverCommand command, DataProductInitializationApproverPresenter presenter) {
        DataProductInitializationApproverNotificationOutboundPort notificationPort = new DataProductInitializationApproverNotificationOutboundPortImpl(notificationClient, dataProductMapper);
        return new DataProductInitializationApprover(command, presenter, notificationPort);
    }
}

