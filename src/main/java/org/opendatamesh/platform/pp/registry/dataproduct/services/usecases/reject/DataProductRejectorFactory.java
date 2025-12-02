package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductRejectorFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductRejector(DataProductRejectCommand command, DataProductRejectPresenter presenter) {
        DataProductRejectorPersistenceOutboundPort persistencePort = new DataProductRejectorPersistenceOutboundPortImpl(dataProductsService);
        return new DataProductRejector(command, presenter, persistencePort, transactionalOutboundPort);
    }
}
