package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductFieldsUpdaterFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductFieldsUpdater(DataProductFieldsUpdateCommand command,
                                                  DataProductFieldsUpdatePresenter presenter) {
        DataProductFieldsUpdaterPersistenceOutboundPort persistencePort =
                new DataProductFieldsUpdaterPersistenceOutboundPortImpl(dataProductsService);
        return new DataProductFieldsUpdater(command, presenter, persistencePort, transactionalOutboundPort);
    }
}
