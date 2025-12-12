package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.received.ReceivedEventDataProductApprovedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductApprovedNotificationEventDispatcher implements NotificationEventDispatcher {

    @Autowired
    private DataProductApproverFactory dataProductApproverFactory;
    @Autowired
    private DataProductMapper dataProductMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_APPROVED);
    }

    @Override
    public void dispatchEventToUseCase(NotificationDispatchEventRes event) {
        DataProductRes dataProductRes = getDataProductFromEvent(event);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProductMapper.toEntity(dataProductRes));
        DataProductApprovePresenter presenter = dataProduct -> {
            // No-op: we don't need to return anything for approve
        };
        dataProductApproverFactory.buildDataProductApprover(command, presenter).execute();
    }

    private DataProductRes getDataProductFromEvent(NotificationDispatchEventRes event) {
        ReceivedEventDataProductApprovedRes typedEvent;
        try {
            typedEvent = objectMapper.convertValue(event, ReceivedEventDataProductApprovedRes.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to convert event from JSON to resource ReceivedEventDataProductApprovedRes: " + e.getMessage(), e);
        }

        if (typedEvent == null) {
            throw new BadRequestException("Event conversion resulted in null");
        }

        if (typedEvent.getEventContent() == null) {
            throw new BadRequestException("Missing 'eventContent' field in event");
        }

        DataProductRes dataProductRes = typedEvent.getEventContent().getDataProduct();
        if (dataProductRes == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }

        return dataProductRes;
    }
}

