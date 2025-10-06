package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionApproverFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductVersionApprover(DataProductVersionApproveCommand command, DataProductVersionApprovePresenter presenter) {
        DataProductVersionApproverPersistenceOutboundPort persistencePort = new DataProductVersionApproverPersistenceOutboundPortImpl(dataProductVersionCrudService);
        DataProductVersionApproverNotificationOutboundPort notificationPort = new DataProductVersionApproverNotificationOutboundPortImpl();
        return new DataProductVersionApprover(command, presenter, notificationPort, persistencePort, transactionalOutboundPort);
    }
}
