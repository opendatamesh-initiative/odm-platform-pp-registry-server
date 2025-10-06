package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductApproverFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductApprover(DataProductApproveCommand command, DataProductApprovePresenter presenter) {
        DataProductApproverPersistenceOutboundPort persistencePort = new DataProductApproverPersistenceOutboundPortImpl(dataProductsService);
        DataProductApproverNotificationOutboundPort notificationPort = new DataProductApproverNotificationOutboundPortImpl();
        return new DataProductApprover(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}
