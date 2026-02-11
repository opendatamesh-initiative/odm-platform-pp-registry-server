package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

class DataProductFieldsUpdater implements UseCase {

    private final DataProductFieldsUpdateCommand command;
    private final DataProductFieldsUpdatePresenter presenter;
    private final DataProductFieldsUpdaterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductFieldsUpdater(DataProductFieldsUpdateCommand command,
                             DataProductFieldsUpdatePresenter presenter,
                             DataProductFieldsUpdaterPersistenceOutboundPort persistencePort,
                             TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProduct dataProduct = persistencePort.findByUuid(command.getUuid());

            if (StringUtils.hasText(command.displayName())) {
                dataProduct.setDisplayName(command.displayName());
            }
            if (command.description() != null) {
                dataProduct.setDescription(command.description());
            }
            // Full replace of dataProductRepo: set whole object or null
            if (command.dataProductRepo() != null) {
                command.dataProductRepo().setDataProduct(dataProduct);
                dataProduct.setDataProductRepo(command.dataProductRepo());
            } else {
                dataProduct.setDataProductRepo(null);
            }

            dataProduct = persistencePort.save(dataProduct);
            presenter.presentDataProductFieldsUpdated(dataProduct);
        });
    }

    private void validateCommand(DataProductFieldsUpdateCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductFieldsUpdateCommand cannot be null");
        }
        if (!StringUtils.hasText(command.getUuid())) {
            throw new BadRequestException("UUID is required for data product fields update");
        }
    }
}
