package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.received.ReceivedEventDataProductVersionRejectedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionRejectedNotificationEventDispatcher implements NotificationEventDispatcher {

    @Autowired
    private DataProductVersionRejectorFactory dataProductVersionRejectorFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_REJECTED);
    }

    @Override
    public void dispatchEventToUseCase(NotificationDispatchEventRes event) {
        String uuid = getUuidFromEvent(event);
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(uuid);
        DataProductVersionRejectCommand command = new DataProductVersionRejectCommand(dataProductVersion);
        DataProductVersionRejectPresenter presenter = dataProductVersionResult -> {
            // No-op: we don't need to return anything for reject
        };
        dataProductVersionRejectorFactory.buildDataProductVersionRejector(command, presenter).execute();
    }

    private String getUuidFromEvent(NotificationDispatchEventRes event) {
        ReceivedEventDataProductVersionRejectedRes typedEvent;
        try {
            typedEvent = objectMapper.convertValue(event, ReceivedEventDataProductVersionRejectedRes.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to convert event from JSON to resource ReceivedEventDataProductVersionRejectedRes: " + e.getMessage(), e);
        }

        if (typedEvent == null) {
            throw new BadRequestException("Event conversion resulted in null");
        }

        if (typedEvent.getEventContent() == null) {
            throw new BadRequestException("Missing 'eventContent' field in event");
        }

        ReceivedEventDataProductVersionRejectedRes.DataProductVersion dataProductVersion = typedEvent.getEventContent().getDataProductVersion();
        if (dataProductVersion == null) {
            throw new BadRequestException("Missing 'dataProductVersion' field in event content");
        }

        if (dataProductVersion.getUuid() == null) {
            throw new BadRequestException("Missing 'uuid' field in data product version");
        }

        return dataProductVersion.getUuid();
    }
}

