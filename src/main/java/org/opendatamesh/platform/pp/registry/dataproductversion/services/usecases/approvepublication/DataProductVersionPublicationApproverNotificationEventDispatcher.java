package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.received.ReceivedEventDataProductVersionPublicationRequestedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductVersionPublicationApproverNotificationEventDispatcher implements NotificationEventDispatcher {

    @Autowired
    private DataProductVersionPublicationApproverService dataProductVersionPublicationApproverService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED);
    }

    @Override
    public void dispatchEventToUseCase(NotificationDispatchEventRes event) {
        // No need to have a separate use case here, we just need to emit the event
        DataProductVersionRes dataProductVersion = getDataProductVersionFromEvent(event);
        dataProductVersionPublicationApproverService.emitDataProductVersionPublicationApprovedEvent(dataProductVersion);
    }

    private DataProductVersionRes getDataProductVersionFromEvent(NotificationDispatchEventRes event) {
        ReceivedEventDataProductVersionPublicationRequestedRes typedEvent;
        try {
            typedEvent = objectMapper.convertValue(event, ReceivedEventDataProductVersionPublicationRequestedRes.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to convert event from JSON to resource ReceivedEventDataProductVersionPublicationRequestedRes: " + e.getMessage(), e);
        }

        if (typedEvent == null) {
            throw new BadRequestException("Event conversion resulted in null");
        }

        if (typedEvent.getContent() == null) {
            throw new BadRequestException("Missing 'content' field in event");
        }

        DataProductVersionRes dataProductVersionRes = typedEvent.getContent().getDataProductVersion();
        if (dataProductVersionRes == null) {
            throw new BadRequestException("Missing 'dataProductVersion' field in event content");
        }

        return dataProductVersionRes;
    }
}

