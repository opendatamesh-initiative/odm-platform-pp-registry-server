package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductDeleterFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductDeleter(DataProductDeleteCommand command, DataProductDeletePresenter presenter) {
        DataProductDeleterPersistenceOutboundPort persistencePort = new DataProductDeleterPersistenceOutboundPortImpl(
                dataProductsService);
        DataProductDeleterNotificationOutboundPort notificationPort = new DataProductDeleterNotificationOutboundPortImpl();
        return new DataProductDeleter(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}

