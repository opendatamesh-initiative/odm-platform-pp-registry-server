package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.PENDING;

class DataProductInitializer implements UseCase {
    private final DataProductInitCommand command;
    private final DataProductInitPresenter presenter;

    private final DataProductInitializerNotificationOutboundPort notificationsPort;
    private final DataProductInitializerPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductInitializer(DataProductInitCommand command,
                           DataProductInitPresenter presenter,
                           DataProductInitializerNotificationOutboundPort notificationsPort,
                           DataProductInitializerPersistenceOutboundPort persistencePort, TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
        this.persistencePort = persistencePort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProduct dataProduct = command.dataProduct();
            handleExistentDataProduct(dataProduct);

            dataProduct.setValidationState(PENDING);
            dataProduct = persistencePort.save(dataProduct);

            notificationsPort.emitDataProductInitializationRequested(dataProduct);
            presenter.presentDataProductInitialized(dataProduct);
        });
    }

    private void handleExistentDataProduct(DataProduct dataProduct) {
        Optional<DataProduct> existentDataProduct = persistencePort.findByFqn(dataProduct.getFqn());

        if (existentDataProduct.isPresent()) {
            switch (existentDataProduct.get().getValidationState()) {
                case PENDING ->
                        throw new BadRequestException("Impossible to init a data product already existent and in PENDING validation state.");
                case APPROVED ->
                        throw new BadRequestException("Impossible to init a data product already existent and APPROVED.");
                case REJECTED -> persistencePort.delete(existentDataProduct.get());
                default ->
                        throw new IllegalStateException(String.format("DataProductInitializer use case, unexpected data product validation state: %s", dataProduct.getValidationState()));
            }
        }
    }

    private void validateCommand(DataProductInitCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductInitCommand cannot be null");
        }

        if (command.dataProduct() == null) {
            throw new BadRequestException("DataProduct cannot be null");
        }

        DataProduct dataProduct = command.dataProduct();

        // Validate FQN is present - required for finding existing data products in persistence port
        if (!StringUtils.hasText(dataProduct.getFqn())) {
            throw new BadRequestException("FQN is required for data product initialization");
        }
    }
}
