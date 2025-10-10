package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionRejectorFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductVersionRejector(DataProductVersionRejectCommand command, DataProductVersionRejectPresenter presenter) {
        DataProductVersionRejectorPersistenceOutboundPort persistencePort = new DataProductVersionRejectorPersistenceOutboundPortImpl(dataProductVersionCrudService);
        return new DataProductVersionRejector(command, presenter, persistencePort, transactionalOutboundPort);
    }
}
