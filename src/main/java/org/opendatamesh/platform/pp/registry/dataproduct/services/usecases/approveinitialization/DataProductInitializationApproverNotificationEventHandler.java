package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.received.ReceivedEventDataProductInitializationRequestedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "odm.product-plane.policy-service.active", havingValue = "false", matchIfMissing = true)
public class DataProductInitializationApproverNotificationEventHandler implements NotificationEventHandler {

    @Autowired
    private DataProductInitializationApproverService dataProductInitializationApproverService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_REQUESTED);
    }

    @Override
    public void handleEvent(NotificationDispatchEventRes event) {
        // No need to have a separate use case here, we just need to emit the event
        DataProductRes dataProduct = getDataProductFromEvent(event);
        dataProductInitializationApproverService.emitDataProductInitializationApprovedEvent(dataProduct);
    }

    private DataProductRes getDataProductFromEvent(NotificationDispatchEventRes event) {
        ReceivedEventDataProductInitializationRequestedRes typedEvent;
        try {
            typedEvent = objectMapper.convertValue(event, ReceivedEventDataProductInitializationRequestedRes.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to convert event from JSON to resource ReceivedEventDataProductInitializationRequestedRes: " + e.getMessage(), e);
        }

        if (typedEvent == null) {
            throw new BadRequestException("Event conversion resulted in null");
        }

        if (typedEvent.getEventContent() == null) {
            throw new BadRequestException("Missing 'content' field in event");
        }

        DataProductRes dataProductRes = typedEvent.getEventContent().getDataProduct();
        if (dataProductRes == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }

        return dataProductRes;
    }
}

