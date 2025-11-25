package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.Optional;

class DataProductDeleter implements UseCase {
    private final DataProductDeleteCommand command;
    private final DataProductDeletePresenter presenter;
    private final DataProductDeleterNotificationOutboundPort notificationsPort;
    private final DataProductDeleterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductDeleter(DataProductDeleteCommand command,
                       DataProductDeletePresenter presenter,
                       DataProductDeleterNotificationOutboundPort notificationsPort,
                       DataProductDeleterPersistenceOutboundPort persistencePort,
                       TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand();

        transactionalPort.doInTransaction(() -> {
            DataProduct dataProduct = findDataProduct(command);

            // Delete the data product - database cascade will automatically delete all associated versions
            persistencePort.delete(dataProduct);

            notificationsPort.emitDataProductDeleted(dataProduct);
            presenter.presentDataProductDeleted(dataProduct);
        });
    }

    private DataProduct findDataProduct(DataProductDeleteCommand command) {
        // Prefer UUID if provided, otherwise use FQN
        if (StringUtils.hasText(command.dataProductUuid())) {
            return persistencePort.findByUuid(command.dataProductUuid());
        }
        
        Optional<DataProduct> dataProductOpt = persistencePort.findByFqn(command.dataProductFqn());
        if (dataProductOpt.isEmpty()) {
            throw new NotFoundException(
                    String.format("Data Product with FQN '%s' not found", command.dataProductFqn()));
        }
        return dataProductOpt.get();
    }

    private void validateCommand() {
        if (command == null) {
            throw new BadRequestException("DataProductDeleteCommand cannot be null");
        }

        // At least one of uuid or fqn must be provided
        if (!StringUtils.hasText(command.dataProductUuid()) && !StringUtils.hasText(command.dataProductFqn())) {
            throw new BadRequestException("Either dataProductUuid or dataProductFqn must be provided");
        }
    }
}

