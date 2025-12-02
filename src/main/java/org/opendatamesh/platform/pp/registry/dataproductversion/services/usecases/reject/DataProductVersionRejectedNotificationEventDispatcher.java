package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionRejectedNotificationEventDispatcher implements NotificationEventDispatcher {

    @Autowired
    private DataProductVersionRejectorFactory dataProductVersionRejectorFactory;
    @Autowired
    private DataProductVersionMapper dataProductVersionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsNotificationEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED);
    }

    @Override
    public void dispatchNotificationToUseCase(NotificationDispatchRes notification) {
        DataProductVersionRes dataProductVersionRes = getDataProductVersionResFromNotification(notification);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersionMapper.toEntity(dataProductVersionRes));
        DataProductVersionRejectPresenter presenter = dataProductVersion -> {
            // No-op: we don't need to return anything for reject
        };
        dataProductVersionRejectorFactory.buildDataProductVersionRejector(command, presenter).execute();
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

