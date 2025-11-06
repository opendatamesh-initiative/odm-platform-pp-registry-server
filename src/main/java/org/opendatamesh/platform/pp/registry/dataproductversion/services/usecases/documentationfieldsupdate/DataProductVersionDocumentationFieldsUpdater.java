package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

class DataProductVersionDocumentationFieldsUpdater implements UseCase {
    private final DataProductVersionDocumentationFieldsUpdateCommand command;
    private final DataProductVersionDocumentationFieldsUpdatePresenter presenter;

    private final DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductVersionDocumentationFieldsUpdater(DataProductVersionDocumentationFieldsUpdateCommand command,
                                                 DataProductVersionDocumentationFieldsUpdatePresenter presenter,
                                                 DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort persistencePort,
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
            DataProductVersion dataProductVersion = persistencePort.findByUuid(command.dataProductVersion().getUuid());

            if (dataProductVersion == null) {
                throw new BadRequestException("Data Product Version with UUID " + command.dataProductVersion().getUuid() + " does not exist");
            }

            if (StringUtils.hasText(command.dataProductVersion().getName())) {
                dataProductVersion.setName(command.dataProductVersion().getName());
            }
            if (StringUtils.hasText(command.dataProductVersion().getDescription())) {
                dataProductVersion.setDescription(command.dataProductVersion().getDescription());
            }
            if (StringUtils.hasText(command.dataProductVersion().getUpdatedBy())) {
                dataProductVersion.setUpdatedBy(command.dataProductVersion().getUpdatedBy());
            }

            dataProductVersion = persistencePort.save(dataProductVersion);
            presenter.presentDataProductVersionDocumentationFieldsUpdated(dataProductVersion);
        });
    }

    private void validateCommand(DataProductVersionDocumentationFieldsUpdateCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionDocumentationFieldsUpdateCommand cannot be null");
        }
        if (command.dataProductVersion() == null) {
            throw new BadRequestException("DataProductVersion cannot be null");
        }
        DataProductVersion dataProductVersion = command.dataProductVersion();
        if (!StringUtils.hasText(dataProductVersion.getUuid())) {
            throw new BadRequestException("UUID is required for data product version documentation fields update");
        }
        if (!StringUtils.hasText(dataProductVersion.getName())) {
            throw new BadRequestException("Version name is required for data product version documentation fields update");
        }
        if(!StringUtils.hasText(dataProductVersion.getUpdatedBy())){
            throw new BadRequestException("User is required for data product version documentation fields update");
        }
        if (dataProductVersion.getContent() == null) {
            throw new BadRequestException("Missing Data Product Version content");
        }
        if (!StringUtils.hasText(dataProductVersion.getTag())) {
            throw new BadRequestException("Missing Data Product Version tag");
        }
    }

}
