package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.APPROVED;
import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.PENDING;

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
            DataProduct dataProduct = persistencePort.findByUuid(command.dataProduct().getUuid());

            if (!PENDING.equals(dataProduct.getValidationState())) {
                throw new BadRequestException(String.format("Data Product %s can be approved only if in PENDING state", command.dataProduct().getFqn()));
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

        // Validate uuid is present
        if (!StringUtils.hasText(dataProduct.getUuid())) {
            throw new BadRequestException("UUID is required for data product approval");
        }
    }
}
