package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.PENDING;
import static org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState.REJECTED;

class DataProductRejector implements UseCase {
    private final DataProductRejectCommand command;
    private final DataProductRejectPresenter presenter;
    private final DataProductRejectorPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductRejector(DataProductRejectCommand command,
                        DataProductRejectPresenter presenter,
                        DataProductRejectorPersistenceOutboundPort persistencePort,
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
            DataProduct dataProduct = persistencePort.findByUuid(command.dataProduct().getUuid());

            if (!PENDING.equals(dataProduct.getValidationState())) {
                throw new BadRequestException(String.format("Data Product %s can be rejected only if in PENDING state", command.dataProduct().getFqn()));
            }

            dataProduct.setValidationState(REJECTED);
            dataProduct = persistencePort.save(dataProduct);

            presenter.presentDataProductRejected(dataProduct);
        });
    }

    private void validateCommand(DataProductRejectCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductRejectCommand cannot be null");
        }

        if (command.dataProduct() == null) {
            throw new BadRequestException("DataProduct cannot be null");
        }

        DataProduct dataProduct = command.dataProduct();

        if (!StringUtils.hasText(dataProduct.getUuid())) {
            throw new BadRequestException("Uuid is required for data product rejection");
        }
    }
}
