package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import java.util.Collections;
import java.util.List;


import org.opendatamesh.dpds.parser.IdentifierStrategyFactory;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class NotificationEventHandlerDpInitializationRequested implements NotificationEventHandler {
    private static final EventTypeRes SUPPORTED_EVENT = EventTypeRes.DATA_PRODUCT_INITIALIZATION_REQUESTED;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

        try {
            String dataProductId = extractDataProductId(dataProductInitEvent);
            PolicyResPolicyEvaluationRequest evaluationRequest = buildPolicyEvaluationRequestResource(dataProductInitEvent, dataProductId);
            PolicyResValidationResponse validationResponse = policyClient.validateInput(evaluationRequest, true);

            Object responseEvent = validationResponseToEvent(validationResponse, dataProductInitEvent);
            notificationClient.notifyEvent(responseEvent);
        } catch (ClientException e) {
            log.warn("Policy client failed for data product initialization, notifying as rejected: {}", e.getMessage(), e);
            EventEmittedDataProductInitializationRejected rejectEvent = buildRejectEvent(dataProductInitEvent);
            notificationClient.notifyEvent(objectMapper.valueToTree(rejectEvent));
        }
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

        RegistryV1DataProductEventState eventState = buildAfterStateDataProductVersion(event.getEventContent().getDataProduct());
        evaluationRequest.setAfterState(objectMapper.valueToTree(eventState));
        evaluationRequest.setCurrentState(null);
        return evaluationRequest;
    }

    /**
     * Builds afterState in policy client event format: { "dataProductVersion": { "info": { "fullyQualifiedName", "description", "domain", "contactPoints": [] }, "tags": [] } }
     */
    private RegistryV1DataProductEventState buildAfterStateDataProductVersion(EventReceivedDataProductInitializationRequested.DataProductRes dataProductV2) {
        RegistryV1DataProductVersionInfo info = new RegistryV1DataProductVersionInfo();
        info.setFullyQualifiedName(dataProductV2.getFqn() != null ? dataProductV2.getFqn() : "");
        info.setDescription(dataProductV2.getDescription() != null ? dataProductV2.getDescription() : "");
        info.setDomain(dataProductV2.getDomain() != null ? dataProductV2.getDomain() : "");
        info.setContactPoints(Collections.emptyList());

        RegistryV1DataProductVersion dataProductVersion = new RegistryV1DataProductVersion();
        dataProductVersion.setInfo(info);
        dataProductVersion.setTags(Collections.emptyList());

        RegistryV1DataProductEventState eventState = new RegistryV1DataProductEventState();
        eventState.setDataProductVersion(dataProductVersion);
        return eventState;
    }

    private String extractDataProductId(EventReceivedDataProductInitializationRequested dataProductInitEvent) {
        //P.A.!! Policy result are expected to reference Data Product using OLD identifier (generated from fqn)
        return IdentifierStrategyFactory.getDefault().getId(dataProductInitEvent.getEventContent().getDataProduct().getFqn());
    }

    /**
     * Event state shape for policy client: afterState has dataProductVersion with info and tags.
     */
    public static class RegistryV1DataProductEventState {
        private RegistryV1DataProductVersion dataProductVersion;

        public RegistryV1DataProductVersion getDataProductVersion() {
            return dataProductVersion;
        }

        public void setDataProductVersion(RegistryV1DataProductVersion dataProductVersion) {
            this.dataProductVersion = dataProductVersion;
        }
    }

    public static class RegistryV1DataProductVersion {
        private RegistryV1DataProductVersionInfo info;
        private List<String> tags;

        public RegistryV1DataProductVersionInfo getInfo() {
            return info;
        }

        public void setInfo(RegistryV1DataProductVersionInfo info) {
            this.info = info;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class RegistryV1DataProductVersionInfo {
        private String fullyQualifiedName;
        private String description;
        private String domain;
        private List<Object> contactPoints;

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

        public List<Object> getContactPoints() {
            return contactPoints;
        }

        public void setContactPoints(List<Object> contactPoints) {
            this.contactPoints = contactPoints;
        }
    }
}
