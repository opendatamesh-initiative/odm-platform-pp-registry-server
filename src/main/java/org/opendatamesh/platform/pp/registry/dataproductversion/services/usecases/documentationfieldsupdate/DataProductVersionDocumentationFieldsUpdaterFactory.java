package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionDocumentationFieldsUpdaterFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductVersionDocumentationFieldsUpdater(DataProductVersionDocumentationFieldsUpdateCommand command, DataProductVersionDocumentationFieldsUpdatePresenter presenter) {
        DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort = new DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPortImpl(dataProductVersionCrudService);
        return new DataProductVersionDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalOutboundPort);
    }
}
