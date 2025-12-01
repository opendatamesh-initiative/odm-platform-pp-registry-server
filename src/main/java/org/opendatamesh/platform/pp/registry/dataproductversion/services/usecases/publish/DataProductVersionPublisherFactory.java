package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionPublisherFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private DataProductVersionsQueryService dataProductVersionsQueryService;
    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;
    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private DataProductVersionMapper dataProductVersionMapper;

    public UseCase buildDataProductVersionPublisher(DataProductVersionPublishCommand command, DataProductVersionPublishPresenter presenter) {
        DataProductVersionPublisherDataProductVersionPersistenceOutboundPort dataProductVersionPersistencePort = new DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl(dataProductVersionCrudService, dataProductVersionsQueryService);
        DataProductVersionPublisherDataProductPersistenceOutboundPort dataProductPersistencePort = new DataProductVersionPublisherDataProductPersistenceOutboundPortImpl(dataProductsService);
        DataProductVersionPublisherNotificationOutboundPort notificationPort = new DataProductVersionPublisherNotificationOutboundPortImpl(notificationClient, dataProductVersionMapper);
        return new DataProductVersionPublisher(command, presenter, notificationPort, dataProductVersionPersistencePort, dataProductPersistencePort, transactionalOutboundPort);
    }
}
