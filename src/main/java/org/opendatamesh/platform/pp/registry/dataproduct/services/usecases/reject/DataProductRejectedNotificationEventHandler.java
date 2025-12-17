package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events.received.ReceivedEventDataProductRejectedRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes.NotificationDispatchEventRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductRejectedNotificationEventHandler implements NotificationEventHandler {

    @Autowired
    private DataProductRejectorFactory dataProductRejectorFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_REJECTED);
    }

    @Override
    public void handleEvent(NotificationDispatchEventRes event) {
        String uuid = getUuidFromEvent(event);
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(uuid);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProduct);
        DataProductRejectPresenter presenter = dataProductResult -> {
            // No-op: we don't need to return anything for reject
        };
        dataProductRejectorFactory.buildDataProductRejector(command, presenter).execute();
    }

    private String getUuidFromEvent(NotificationDispatchEventRes event) {
        ReceivedEventDataProductRejectedRes typedEvent;
        try {
            typedEvent = objectMapper.convertValue(event, ReceivedEventDataProductRejectedRes.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Failed to convert event from JSON to resource ReceivedEventDataProductRejectedRes: " + e.getMessage(), e);
        }

        if (typedEvent == null) {
            throw new BadRequestException("Event conversion resulted in null");
        }

        if (typedEvent.getEventContent() == null) {
            throw new BadRequestException("Missing 'eventContent' field in event");
        }

        ReceivedEventDataProductRejectedRes.DataProduct dataProduct = typedEvent.getEventContent().getDataProduct();
        if (dataProduct == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }

        if (dataProduct.getUuid() == null) {
            throw new BadRequestException("Missing 'uuid' field in data product");
        }

        return dataProduct.getUuid();
    }
}

