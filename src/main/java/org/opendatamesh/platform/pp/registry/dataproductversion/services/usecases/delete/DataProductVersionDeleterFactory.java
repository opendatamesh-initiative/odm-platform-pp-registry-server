package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

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
public class DataProductVersionDeleterFactory {

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

    public UseCase buildDataProductVersionDeleter(DataProductVersionDeleteCommand command, DataProductVersionDeletePresenter presenter) {
        DataProductVersionDeleterPersistenceOutboundPort persistencePort = new DataProductVersionDeleterPersistenceOutboundPortImpl(
                dataProductVersionCrudService, dataProductVersionsQueryService, dataProductsService);
        DataProductVersionDeleterNotificationOutboundPort notificationPort = new DataProductVersionDeleterNotificationOutboundPortImpl(notificationClient);
        return new DataProductVersionDeleter(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}

