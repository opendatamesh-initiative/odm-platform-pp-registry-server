package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.received.ReceivedEventDataProductInitializationRequestedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductInitializationApproverNotificationEventDispatcher implements NotificationEventDispatcher {

    @Autowired
    private DataProductInitializationApproverFactory dataProductInitializationApproverFactory;
    @Autowired
    private DataProductMapper dataProductMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_REQUESTED);
    }

    @Override
    public void dispatchEventToUseCase(NotificationDispatchEventRes event) {
        DataProductRes dataProductRes = getDataProductFromEvent(event);
        DataProductInitializationApproverCommand command = new DataProductInitializationApproverCommand(dataProductMapper.toEntity(dataProductRes));
        DataProductInitializationApproverPresenter presenter = dataProduct -> {
            // No-op: we don't need to return anything for approve
        };
        dataProductInitializationApproverFactory.buildDataProductInitializationApprover(command, presenter).execute();
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

        if (typedEvent.getContent() == null) {
            throw new BadRequestException("Missing 'content' field in event");
        }

        DataProductRes dataProductRes = typedEvent.getContent().getDataProduct();
        if (dataProductRes == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }

        return dataProductRes;
    }
}

