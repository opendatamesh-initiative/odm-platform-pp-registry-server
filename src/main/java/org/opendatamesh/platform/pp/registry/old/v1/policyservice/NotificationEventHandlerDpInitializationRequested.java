package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationEventHandlerDpInitializationRequested implements NotificationEventHandler {
    private static final EventTypeRes SUPPORTED_EVENT = EventTypeRes.DATA_PRODUCT_INITIALIZATION_REQUESTED;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final NotificationClient notificationClient;
    private final PolicyClientV1 policyClient;

    @Autowired
    public NotificationEventHandlerDpInitializationRequested(NotificationClient notificationClient, PolicyClientV1 policyClient) {
        this.notificationClient = notificationClient;
        this.policyClient = policyClient;
    }

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return SUPPORTED_EVENT.equals(eventType);
    }

    @Override
    public void handleEvent(NotificationDispatchRes.NotificationDispatchEventRes event) {
        EventReceivedDataProductInitializationRequested dataProductInitEvent = objectMapper.convertValue(event, EventReceivedDataProductInitializationRequested.class);

        String dataProductId = extractDataProductId(dataProductInitEvent);
        PolicyResPolicyEvaluationRequest evaluationRequest = buildPolicyEvaluationRequestResource(dataProductInitEvent, dataProductId);
        PolicyResValidationResponse validationResponse = policyClient.validateInput(evaluationRequest, true);

        Object responseEvent = validationResponseToEvent(validationResponse, dataProductInitEvent);
        notificationClient.notifyEvent(responseEvent);
    }

    private Object validationResponseToEvent(PolicyResValidationResponse validationResponse, EventReceivedDataProductInitializationRequested originalEvent) {
        // Check if any blocking policies failed
        boolean hasBlockingPolicyFailure = hasBlockingPolicyFailure(validationResponse);

        if (!hasBlockingPolicyFailure) {
            return objectMapper.valueToTree(buildApproveEvent(originalEvent));
        } else {
            return objectMapper.valueToTree(buildRejectEvent(originalEvent));
        }
    }

    private EventEmittedDataProductInitializationRejected buildRejectEvent(EventReceivedDataProductInitializationRequested originalEvent) {
        EventEmittedDataProductInitializationRejected rejectedEvent = new EventEmittedDataProductInitializationRejected();
        rejectedEvent.setResourceIdentifier(originalEvent.getResourceIdentifier());

        EventEmittedDataProductInitializationRejected.EventContent eventContent =
                new EventEmittedDataProductInitializationRejected.EventContent();
        EventEmittedDataProductInitializationRejected.DataProductRes dataProduct =
                new EventEmittedDataProductInitializationRejected.DataProductRes();
        dataProduct.setUuid(originalEvent.getEventContent().getDataProduct().getUuid());
        dataProduct.setFqn(originalEvent.getEventContent().getDataProduct().getFqn());
        eventContent.setDataProduct(dataProduct);
        rejectedEvent.setEventContent(eventContent);

        return rejectedEvent;
    }

    private EventEmittedDataProductInitializationApproved buildApproveEvent(EventReceivedDataProductInitializationRequested originalEvent) {
        EventEmittedDataProductInitializationApproved approvedEvent = new EventEmittedDataProductInitializationApproved();
        approvedEvent.setResourceIdentifier(originalEvent.getResourceIdentifier());

        EventEmittedDataProductInitializationApproved.EventContent eventContent =
                new EventEmittedDataProductInitializationApproved.EventContent();
        EventEmittedDataProductInitializationApproved.DataProductRes dataProduct =
                new EventEmittedDataProductInitializationApproved.DataProductRes();
        dataProduct.setUuid(originalEvent.getEventContent().getDataProduct().getUuid());
        dataProduct.setFqn(originalEvent.getEventContent().getDataProduct().getFqn());
        eventContent.setDataProduct(dataProduct);
        approvedEvent.setEventContent(eventContent);

        return approvedEvent;
    }

    private boolean hasBlockingPolicyFailure(PolicyResValidationResponse validationResponse) {
        boolean hasBlockingPolicyFailure = false;
        if (validationResponse.getPolicyResults() != null) {
            for (PolicyResPolicyEvaluationResult policyResult : validationResponse.getPolicyResults()) {
                if (Boolean.FALSE.equals(policyResult.getResult()) &&
                        policyResult.getPolicy() != null &&
                        Boolean.TRUE.equals(policyResult.getPolicy().getBlockingFlag())) {
                    hasBlockingPolicyFailure = true;
                    break;
                }
            }
        }
        return hasBlockingPolicyFailure;
    }

    private PolicyResPolicyEvaluationRequest buildPolicyEvaluationRequestResource(EventReceivedDataProductInitializationRequested event, String dataProductId) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_CREATION);
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setDataProductId(dataProductId);

        RegistryV1DataProductResource registryV1DataProductResource = mapDataProductV2toV1(event.getEventContent().getDataProduct());

        RegistryV1DataProductEventState eventState = new RegistryV1DataProductEventState();
        eventState.setDataProduct(registryV1DataProductResource);

        evaluationRequest.setAfterState(objectMapper.valueToTree(eventState));
        // Set currentState to null (as per test scenario - missing currentState is handled gracefully)
        evaluationRequest.setCurrentState(null);
        return evaluationRequest;
    }

    private RegistryV1DataProductResource mapDataProductV2toV1(EventReceivedDataProductInitializationRequested.DataProductRes dataProductV2) {
        RegistryV1DataProductResource registryV1DataProductResource = new RegistryV1DataProductResource();
        registryV1DataProductResource.setId(dataProductV2.getUuid());
        registryV1DataProductResource.setDescription(dataProductV2.getDescription());
        registryV1DataProductResource.setDomain(dataProductV2.getDomain());
        registryV1DataProductResource.setFullyQualifiedName(dataProductV2.getFqn());
        return registryV1DataProductResource;
    }

    private String extractDataProductId(EventReceivedDataProductInitializationRequested dataProductInitEvent) {
        String dataProductId = dataProductInitEvent.getResourceIdentifier();
        if (dataProductId == null || dataProductId.isEmpty()) {
            throw new IllegalStateException("Missing resourceIdentifier in notification event");
        }
        return dataProductId;
    }


    public class RegistryV1DataProductEventState {

        private RegistryV1DataProductResource dataProduct;

        public RegistryV1DataProductEventState() {
        }

        public RegistryV1DataProductEventState(RegistryV1DataProductResource dataProduct) {
            this.dataProduct = dataProduct;
        }

        public RegistryV1DataProductResource getDataProduct() {
            return dataProduct;
        }

        public void setDataProduct(RegistryV1DataProductResource dataProduct) {
            this.dataProduct = dataProduct;
        }
    }

    public class RegistryV1DataProductResource {
        private String id;
        private String fullyQualifiedName;
        private String description;
        private String domain;

        public RegistryV1DataProductResource() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }

        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

}
