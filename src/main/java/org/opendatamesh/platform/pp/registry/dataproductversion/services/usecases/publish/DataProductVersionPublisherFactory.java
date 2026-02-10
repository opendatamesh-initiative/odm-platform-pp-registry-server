package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DataProductVersionPublisherFactory {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private DataProductVersionsQueryService dataProductVersionsQueryService;
    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;
    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private DataProductVersionMapper dataProductVersionMapper;
    @Autowired
    private DescriptorValidatorFactory descriptorValidatorFactory;

    public UseCase buildDataProductVersionPublisher(DataProductVersionPublishCommand command, DataProductVersionPublishPresenter presenter) {
        DataProductVersionPublisherDataProductVersionPersistenceOutboundPort dataProductVersionPersistencePort = new DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl(dataProductVersionCrudService, dataProductVersionsQueryService);
        DataProductVersionPublisherDataProductPersistenceOutboundPort dataProductPersistencePort = new DataProductVersionPublisherDataProductPersistenceOutboundPortImpl(dataProductsService);
        DataProductVersionPublisherNotificationOutboundPort notificationPort = new DataProductVersionPublisherNotificationOutboundPortImpl(notificationClient, dataProductVersionMapper);
        DataProductVersionPublisherDescriptorOutboundPort descriptorHandlerPort = buildDescriptorPort(command.dataProductVersion());
        return new DataProductVersionPublisher(command, presenter, notificationPort, dataProductVersionPersistencePort, dataProductPersistencePort, descriptorHandlerPort, transactionalOutboundPort);
    }

    private DataProductVersionPublisherDescriptorOutboundPort buildDescriptorPort(DataProductVersion dataProductVersion) {
        String spec = dataProductVersion.getSpec();
        if (!StringUtils.hasText(spec)) {
            throw new BadRequestException("Data Product Version spec is required to determine the descriptor handler");
        }

        String specVersion = dataProductVersion.getSpecVersion();
        if (!StringUtils.hasText(specVersion)) {
            specVersion = "1.0.0"; // Default spec version
        }

        try {
            DescriptorSpec descriptorSpec = DescriptorSpec.valueOf(spec.toUpperCase());
            if (descriptorSpec == DescriptorSpec.DPDS && specVersion.matches("1.*.*")) {
                return new DataProductVersionPublisherDpdsDescriptorOutboundPort(descriptorValidatorFactory);
            } else {
                throw new BadRequestException(String.format("Unsupported descriptor specification: %s. Currently only 'dpds' is supported.", spec));
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unsupported descriptor specification: %s. Currently only 'dpds' is supported.", spec));
        }
    }
}
