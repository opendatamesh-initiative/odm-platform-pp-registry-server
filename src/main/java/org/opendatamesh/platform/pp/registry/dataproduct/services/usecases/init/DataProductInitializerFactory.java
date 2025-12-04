package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductInitializerFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;
    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private DataProductMapper dataProductMapper;

    public UseCase buildDataProductInitializer(DataProductInitCommand command, DataProductInitPresenter presenter) {
        DataProductInitializerPersistenceOutboundPort persistencePort = new DataProductInitializerPersistenceOutboundPortImpl(dataProductsService);
        DataProductInitializerNotificationOutboundPort notificationPort = new DataProductInitializerNotificationOutboundPortImpl(notificationClient, dataProductMapper);
        return new DataProductInitializer(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}
