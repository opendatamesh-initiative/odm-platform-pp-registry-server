package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionRejectorFactory implements NotificationEventDispatcher {

    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;
    @Autowired
    private DataProductVersionMapper dataProductVersionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UseCase buildDataProductVersionRejector(DataProductVersionRejectCommand command, DataProductVersionRejectPresenter presenter) {
        DataProductVersionRejectorPersistenceOutboundPort persistencePort = new DataProductVersionRejectorPersistenceOutboundPortImpl(dataProductVersionCrudService);
        return new DataProductVersionRejector(command, presenter, persistencePort, transactionalOutboundPort);
    }

    @Override
    public boolean supportsNotificationEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED);
    }

    @Override
    public void dispatchNotificationToUseCase(NotificationDispatchRes notification) {
        DataProductVersionRes dataProductVersionRes = getDataProductVersionResFromNotification(notification);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersionMapper.toEntity(dataProductVersionRes));
        DataProductVersionRejectPresenter presenter = new DataProductVersionRejectPresenter() {
            @Override
            public void presentDataProductVersionRejected(DataProductVersion dataProductVersion) {
                // No-op: we don't need to return anything for approve
            }
        };
        buildDataProductVersionRejector(command, presenter).execute();
    }

    private DataProductVersionRes getDataProductVersionResFromNotification(NotificationDispatchRes notification) {
        JsonNode content = notification.getEvent().getContent();
        JsonNode dataProductVersionNode = content.get("dataProductVersion");
        DataProductVersionRes dataProductVersionRes = null;

        if (dataProductVersionNode == null) {
            throw new BadRequestException("Missing 'dataProductVersion' field in event content");
        }
        try {
            dataProductVersionRes = objectMapper.treeToValue(dataProductVersionNode, DataProductVersionRes.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse dataProductVersion from event content: " + e.getMessage(), e);
        }

        if (dataProductVersionRes == null) {
            throw new BadRequestException("No dataProductVersion found in event content");
        }

        return dataProductVersionRes;
    }

}
