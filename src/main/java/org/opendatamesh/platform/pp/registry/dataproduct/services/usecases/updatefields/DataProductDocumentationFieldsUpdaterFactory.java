package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductDocumentationFieldsUpdaterFactory {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;

    public UseCase buildDataProductDocumentationFieldsUpdater(DataProductDocumentationFieldsUpdateCommand command,
                                                              DataProductFieldsUpdatePresenter presenter) {
        DataProductDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort =
                new DataProductDocumentationFieldsUpdaterPersistenceOutboundPortImpl(dataProductsService);
        return new DataProductDocumentationFieldsUpdater(command, presenter, persistencePort, transactionalOutboundPort);
    }
}
