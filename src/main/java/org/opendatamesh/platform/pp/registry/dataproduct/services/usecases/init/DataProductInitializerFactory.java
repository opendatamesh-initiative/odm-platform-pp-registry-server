package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
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

    public UseCase buildDataProductInitializer(DataProductInitCommand command, DataProductInitPresenter presenter) {
        DataProductInitializerPersistenceOutboundPort persistencePort = new DataProductInitializerPersistenceOutboundPortImpl(dataProductsService);
        DataProductInitializerNotificationOutboundPort notificationPort = new DataProductInitializerNotificationOutboundPortImpl();
        return new DataProductInitializer(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}
