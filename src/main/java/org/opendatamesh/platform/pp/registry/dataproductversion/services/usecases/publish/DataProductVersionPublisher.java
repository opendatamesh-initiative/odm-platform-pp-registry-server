package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.Optional;


class DataProductVersionPublisher implements UseCase {
    private final DataProductVersionPublishCommand command;
    private final DataProductVersionPublishPresenter presenter;

    private final DataProductVersionPublisherNotificationOutboundPort notificationsPort;
    private final DataProductVersionPublisherDataProductVersionPersistenceOutboundPort dataProductVersionPersistencePort;
    private final DataProductVersionPublisherDataProductPersistenceOutboundPort dataProductPersistencePort;
    private final DataProductVersionPublisherDescriptorOutboundPort descriptorHandlerPort;
    private final TransactionalOutboundPort transactionalPort;

    DataProductVersionPublisher(DataProductVersionPublishCommand command,
                                DataProductVersionPublishPresenter presenter,
                                DataProductVersionPublisherNotificationOutboundPort notificationsPort,
                                DataProductVersionPublisherDataProductVersionPersistenceOutboundPort dataProductVersionPersistencePort, DataProductVersionPublisherDataProductPersistenceOutboundPort dataProductPersistencePort,
                                DataProductVersionPublisherDescriptorOutboundPort descriptorHandlerPort,
                                TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.notificationsPort = notificationsPort;
        this.dataProductVersionPersistencePort = dataProductVersionPersistencePort;
        this.dataProductPersistencePort = dataProductPersistencePort;
        this.descriptorHandlerPort = descriptorHandlerPort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);

        transactionalPort.doInTransaction(() -> {
            DataProductVersion dataProductVersion = command.dataProductVersion();

            verifyDataProductIsApproved(command.dataProductVersion().getDataProductUuid());

            String spec = dataProductVersion.getSpec();
            String specVersion = dataProductVersion.getSpecVersion() != null ? dataProductVersion.getSpecVersion() : "1.0.0";
            descriptorHandlerPort.validateDescriptor(spec, specVersion, dataProductVersion.getContent());
            JsonNode enrichedContent = descriptorHandlerPort.enrichDescriptorContentIfNeeded(spec, specVersion, dataProductVersion.getContent());
            dataProductVersion.setContent(enrichedContent);
            String versionNumber = descriptorHandlerPort.extractVersionNumber(enrichedContent);
            dataProductVersion.setVersionNumber(versionNumber);

            handleExistentDataProductVersion(dataProductVersion);

            dataProductVersion.setValidationState(DataProductVersionValidationState.PENDING);
            dataProductVersion = dataProductVersionPersistencePort.save(dataProductVersion);

            DataProductVersion previousDataProductVersion = findPreviousDataProductVersion(dataProductVersion);
            notificationsPort.emitDataProductVersionPublicationRequested(dataProductVersion, previousDataProductVersion);
            presenter.presentDataProductVersionPublished(dataProductVersion);
        });
    }

    private void verifyDataProductIsApproved(String dataProductUuid) {
        DataProduct dataProduct = dataProductPersistencePort.findByUuid(dataProductUuid);
        if (!DataProductValidationState.APPROVED.equals(dataProduct.getValidationState())) {
            throw new BadRequestException(String.format("Data Product %s must be APPROVED in order to publish a Data Product Version.", dataProduct.getFqn()));
        }
    }

    private void handleExistentDataProductVersion(DataProductVersion dataProductVersion) {
        Optional<DataProductVersionShort> existentDataProductVersion = dataProductVersionPersistencePort.findByDataProductUuidAndVersionNumber(dataProductVersion.getDataProductUuid(), dataProductVersion.getVersionNumber());

        if (existentDataProductVersion.isPresent()) {
            switch (existentDataProductVersion.get().getValidationState()) {
                case PENDING ->
                        throw new BadRequestException("Impossible to publish a data product version already existent and in PENDING validation state.");
                case APPROVED ->
                        throw new BadRequestException("Impossible to publish a data product version already existent and APPROVED.");
                case REJECTED -> dataProductVersionPersistencePort.delete(existentDataProductVersion.get().getUuid());
                default ->
                        throw new IllegalStateException(String.format("DataProductVersionPublisher use case, unexpected data product version validation state: %s", dataProductVersion.getValidationState()));
            }
        }
    }

    private DataProductVersion findPreviousDataProductVersion(DataProductVersion dataProductVersion) {
        Optional<DataProductVersionShort> previousVersionShort = dataProductVersionPersistencePort.findLatestByDataProductUuidExcludingUuid(
                dataProductVersion.getDataProductUuid(), dataProductVersion.getUuid());
        
        if (previousVersionShort.isPresent()) {
            return dataProductVersionPersistencePort.findByUuid(previousVersionShort.get().getUuid());
        }
        
        return null;
    }

    private void validateCommand(DataProductVersionPublishCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionPublishCommand cannot be null");
        }
        if (command.dataProductVersion() == null) {
            throw new BadRequestException("DataProductVersion cannot be null");
        }
        DataProductVersion dataProductVersion = command.dataProductVersion();
        if (!StringUtils.hasText(dataProductVersion.getDataProductUuid())) {
            throw new BadRequestException("Missing DataProduct on DataProductVersion");
        }
        if (!StringUtils.hasText(dataProductVersion.getName())) {
            throw new BadRequestException("Missing Data Product Version name");
        }
        if (dataProductVersion.getContent() == null) {
            throw new BadRequestException("Missing Data Product Version content");
        }
    }

}
