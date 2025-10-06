package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.APPROVED;

class DataProductApprover implements UseCase {
    private final DataProductApproveCommand command;
    private final DataProductApprovePresenter presenter;

    private final DataProductApproverNotificationOutboundPort notificationsPort;
    private final DataProductApproverPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductApprover(DataProductApproveCommand command,
                         DataProductApprovePresenter presenter,
                         DataProductApproverNotificationOutboundPort notificationsPort,
                         DataProductApproverPersistenceOutboundPort persistencePort,
                         TransactionalOutboundPort transactionalPort) {
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
            DataProduct dataProduct = persistencePort.find(command.dataProduct())
                    .orElseThrow(() -> new BadRequestException(String.format("Impossible to approve a data product that does not exist yet. Data Product Fqn: %s", command.dataProduct().getFqn())));

            if (APPROVED.equals(dataProduct.getValidationState())) {
                throw new BadRequestException(String.format("Data Product %s already approved", command.dataProduct().getFqn()));
            }

            dataProduct.setValidationState(APPROVED);
            dataProduct = persistencePort.save(dataProduct);

            notificationsPort.emitDataProductInitialized(dataProduct);
            presenter.presentDataProductApproved(dataProduct);
        });
    }

    private void validateCommand(DataProductApproveCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductApproveCommand cannot be null");
        }

        if (command.dataProduct() == null) {
            throw new BadRequestException("DataProduct cannot be null");
        }

        DataProduct dataProduct = command.dataProduct();

        // Validate FQN is present - required for finding existing data products in persistence port
        if (!StringUtils.hasText(dataProduct.getFqn())) {
            throw new BadRequestException("FQN is required for data product approval");
        }
    }
}
