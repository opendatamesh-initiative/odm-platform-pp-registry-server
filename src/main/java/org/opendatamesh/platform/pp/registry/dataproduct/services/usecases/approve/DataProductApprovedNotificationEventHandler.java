package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.received.ReceivedEventDataProductApprovedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductApprovedNotificationEventHandler implements NotificationEventHandler {

    @Autowired
    private DataProductApproverFactory dataProductApproverFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_APPROVED);
    }

    @Override
    public void handleEvent(NotificationDispatchEventRes event) {
        String uuid = getUuidFromEvent(event);
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(uuid);
        DataProductApproveCommand command = new DataProductApproveCommand(dataProduct);
        DataProductApprovePresenter presenter = dataProductResult -> {
            // No-op: we don't need to return anything for approve
        };
        dataProductApproverFactory.buildDataProductApprover(command, presenter).execute();
    }

    private String getUuidFromEvent(NotificationDispatchEventRes event) {
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

        ReceivedEventDataProductApprovedRes.DataProduct dataProduct = typedEvent.getEventContent().getDataProduct();
        if (dataProduct == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }

        if (dataProduct.getUuid() == null) {
            throw new BadRequestException("Missing 'uuid' field in data product");
        }

        return dataProduct.getUuid();
    }
}

