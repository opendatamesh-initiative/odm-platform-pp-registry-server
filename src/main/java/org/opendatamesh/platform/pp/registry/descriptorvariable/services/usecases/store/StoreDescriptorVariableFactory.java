package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreDescriptorVariableFactory {

    @Autowired
    private DescriptorVariableCrudService descriptorVariableCrudService;
    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildStoreDescriptorVariable(StoreDescriptorVariableCommand command, StoreDescriptorVariablePresenter presenter) {
        StoreDescriptorVariablePersistenceOutboundPort persistencePort = new StoreDescriptorVariablePersistenceOutboundPortImpl(
                descriptorVariableCrudService, dataProductVersionCrudService);
        StoreDescriptorVariableValidationOutboundPort validationPort = new StoreDescriptorVariableValidationOutboundPortImpl();
        return new StoreDescriptorVariable(command, presenter, persistencePort, validationPort, transactionalOutboundPort);
    }
}
