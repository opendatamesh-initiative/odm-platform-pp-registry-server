package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionVariablesResolverFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private DescriptorVariableCrudService descriptorVariableCrudService;

    public UseCase buildResolveDataProductVersion(DataProductVersionVariablesResolverCommand command, DataProductVersionVariablesResolverPresenter presenter) {
        DataProductVersionVariablesResolverPersistenceOutboundPort persistencePort = new DataProductVersionVariablesResolverPersistenceOutboundPortImpl(
                dataProductVersionCrudService, descriptorVariableCrudService);
        DataProductVersionVariablesResolverDescriptorOutboundPort descriptorPort = new DataProductVersionVariablesResolverDescriptorOutboundPortImpl();
        return new DataProductVersionVariablesResolver(command, presenter, persistencePort, descriptorPort);
    }
}
