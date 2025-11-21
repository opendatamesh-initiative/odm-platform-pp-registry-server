package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.Optional;

class DataProductVersionDeleter implements UseCase {
    private final DataProductVersionDeleteCommand command;
    private final DataProductVersionDeletePresenter presenter;
    private final DataProductVersionDeleterNotificationOutboundPort notificationsPort;
    private final DataProductVersionDeleterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;
    private final DataProductsService dataProductsService;

    DataProductVersionDeleter(DataProductVersionDeleteCommand command,
                              DataProductVersionDeletePresenter presenter,
                              DataProductVersionDeleterNotificationOutboundPort notificationsPort,
                              DataProductVersionDeleterPersistenceOutboundPort persistencePort,
                              TransactionalOutboundPort transactionalPort,
                              DataProductsService dataProductsService) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
        this.dataProductsService = dataProductsService;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProductVersion dataProductVersion = findDataProductVersion(command);

            // Delete the data product version
            persistencePort.delete(dataProductVersion);

            notificationsPort.emitDataProductVersionDeleted(dataProductVersion);
            presenter.presentDataProductVersionDeleted(dataProductVersion);
        });
    }

    private DataProductVersion findDataProductVersion(DataProductVersionDeleteCommand command) {
        // Prefer UUID if provided, otherwise use FQN + tag
        if (StringUtils.hasText(command.dataProductVersionUuid())) {
            return persistencePort.findByUuid(command.dataProductVersionUuid());
        } else if (StringUtils.hasText(command.dataProductFqn()) && StringUtils.hasText(command.dataProductVersionTag())) {
            // First find the data product by FQN
            DataProductSearchOptions dataProductFilter = new DataProductSearchOptions();
            dataProductFilter.setFqn(command.dataProductFqn());
            Optional<DataProduct> dataProductOpt = dataProductsService.findAllFiltered(Pageable.ofSize(1), dataProductFilter)
                    .stream()
                    .findFirst();
            
            if (dataProductOpt.isEmpty()) {
                throw new NotFoundException(
                        String.format("Data Product with FQN '%s' not found", command.dataProductFqn()));
            }
            
            DataProduct dataProduct = dataProductOpt.get();
            
            // Then find the version by data product UUID + tag
            Optional<DataProductVersion> versionOpt = persistencePort.findByDataProductUuidAndTag(
                    dataProduct.getUuid(), command.dataProductVersionTag());
            
            if (versionOpt.isEmpty()) {
                throw new NotFoundException(
                        String.format("Data Product Version with tag '%s' not found for Data Product with FQN '%s'",
                                command.dataProductVersionTag(), command.dataProductFqn()));
            }
            
            return versionOpt.get();
        } else {
            // This should not happen due to validation, but handle it anyway
            throw new BadRequestException("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");
        }
    }

    private void validateCommand(DataProductVersionDeleteCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionDeleteCommand cannot be null");
        }

        // Either UUID is provided, or both FQN and tag must be provided
        boolean hasUuid = StringUtils.hasText(command.dataProductVersionUuid());
        boolean hasFqn = StringUtils.hasText(command.dataProductFqn());
        boolean hasTag = StringUtils.hasText(command.dataProductVersionTag());

        if (!hasUuid && !(hasFqn && hasTag)) {
            throw new BadRequestException("Either dataProductVersionUuid or both dataProductFqn and dataProductVersionTag must be provided");
        }
    }
}

