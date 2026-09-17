package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

class DataProductDocumentationFieldsUpdater implements UseCase {

    private final DataProductDocumentationFieldsUpdateCommand command;
    private final DataProductFieldsUpdatePresenter presenter;
    private final DataProductDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductDocumentationFieldsUpdater(DataProductDocumentationFieldsUpdateCommand command,
                                          DataProductFieldsUpdatePresenter presenter,
                                          DataProductDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort,
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

            // Replace additional repos only when the list is present; omit leaves existing extras unchanged.
            if (command.additionalDataProductRepos() != null) {
                replaceAdditionalDataProductRepos(dataProduct, command.additionalDataProductRepos());
            }

            dataProduct = persistencePort.save(dataProduct);
            presenter.presentDataProductFieldsUpdated(dataProduct);
        });
    }

    private void replaceAdditionalDataProductRepos(DataProduct dataProduct, List<DataProductAdditionalRepo> additionalRepos) {
        List<DataProductAdditionalRepo> persistedRepos = dataProduct.getAdditionalDataProductRepos();
        if (persistedRepos == null) {
            persistedRepos = new ArrayList<>();
            dataProduct.setAdditionalDataProductRepos(persistedRepos);
        } else {
            persistedRepos.clear();
        }
        for (DataProductAdditionalRepo additionalRepo : additionalRepos) {
            additionalRepo.setUuid(null);
            additionalRepo.setDataProduct(dataProduct);
            persistedRepos.add(additionalRepo);
        }
    }

    private void validateCommand(DataProductDocumentationFieldsUpdateCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductFieldsUpdateCommand cannot be null");
        }
        if (!StringUtils.hasText(command.getUuid())) {
            throw new BadRequestException("UUID is required for data product fields update");
        }
    }
}
