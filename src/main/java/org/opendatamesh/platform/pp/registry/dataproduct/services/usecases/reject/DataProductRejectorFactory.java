package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventTypeRes;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventDispatcher;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProductRejectorFactory implements NotificationEventDispatcher {

    @Autowired
    private DataProductsService dataProductsService;
    @Autowired
    private TransactionalOutboundPort transactionalOutboundPort;
    @Autowired
    private DataProductMapper dataProductMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UseCase buildDataProductRejector(DataProductRejectCommand command, DataProductRejectPresenter presenter) {
        DataProductRejectorPersistenceOutboundPort persistencePort = new DataProductRejectorPersistenceOutboundPortImpl(dataProductsService);
        return new DataProductRejector(command, presenter, persistencePort, transactionalOutboundPort);
    }

    @Override
    public boolean supportsNotificationEventType(EventTypeRes eventType) {
        return eventType.equals(EventTypeRes.DATA_PRODUCT_INITIALIZATION_REJECTED);
    }

    @Override
    public void dispatchNotificationToUseCase(NotificationDispatchRes notification) {
        DataProductRes dataProductRes = getDataProductResFromNotification(notification);
        DataProductRejectCommand command = new DataProductRejectCommand(dataProductMapper.toEntity(dataProductRes));
        DataProductRejectPresenter presenter = new DataProductRejectPresenter() {
            @Override
            public void presentDataProductRejected(DataProduct dataProduct) {
                // No-op: we don't need to return anything for approve
            }
        };
        buildDataProductRejector(command, presenter).execute();
    }

    private DataProductRes getDataProductResFromNotification(NotificationDispatchRes notification) {
        JsonNode content = notification.getEvent().getContent();
        JsonNode dataProductNode = content.get("dataProduct");
        DataProductRes dataProductRes = null;

        if (dataProductNode == null) {
            throw new BadRequestException("Missing 'dataProduct' field in event content");
        }
        try {
            dataProductRes = objectMapper.treeToValue(dataProductNode, DataProductRes.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse dataProduct from event content: " + e.getMessage(), e);
        }

        if (dataProductRes == null) {
            throw new BadRequestException("No dataProduct found in event content");
        }

        return dataProductRes;
    }
}
