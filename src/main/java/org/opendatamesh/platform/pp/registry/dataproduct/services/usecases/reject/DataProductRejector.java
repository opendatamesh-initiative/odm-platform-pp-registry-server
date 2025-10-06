package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

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
            DataProduct dataProduct = persistencePort.find(command.dataProduct())
                    .orElseThrow(() -> new BadRequestException(String.format("Impossible to reject a data product that does not exist yet. Data Product Fqn: %s", command.dataProduct().getFqn())));

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

        // Validate FQN is present - required for finding existing data products in persistence port
        if (!StringUtils.hasText(dataProduct.getFqn())) {
            throw new BadRequestException("FQN is required for data product rejection");
        }
    }
}
