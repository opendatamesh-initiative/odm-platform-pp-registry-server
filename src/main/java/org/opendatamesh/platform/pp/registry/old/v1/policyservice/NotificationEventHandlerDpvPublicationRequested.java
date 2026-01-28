package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendatamesh.dpds.exceptions.ParseException;
import org.opendatamesh.dpds.location.DescriptorLocation;
import org.opendatamesh.dpds.location.UriLocation;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;
import org.opendatamesh.dpds.parser.DPDSParser;
import org.opendatamesh.dpds.parser.IdentifierStrategyFactory;
import org.opendatamesh.dpds.parser.ParseOptions;
import org.opendatamesh.dpds.parser.ParseResult;
import org.opendatamesh.platform.pp.registry.client.notification.NotificationClient;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class NotificationEventHandlerDpvPublicationRequested implements NotificationEventHandler {
    private static final EventTypeRes SUPPORTED_EVENT = EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final NotificationClient notificationClient;
    private final PolicyClientV1 policyClient;

    @Autowired
    public NotificationEventHandlerDpvPublicationRequested(NotificationClient notificationClient, PolicyClientV1 policyClient) {
        this.notificationClient = notificationClient;
        this.policyClient = policyClient;
    }

    @Override
    public boolean supportsEventType(EventTypeRes eventType) {
        return SUPPORTED_EVENT.equals(eventType);
    }

    @Override
    public void handleEvent(NotificationDispatchRes.NotificationDispatchEventRes event) {
        EventReceivedDataProductVersionPublicationRequested dataProductVersionPublishEvent = objectMapper.convertValue(event, EventReceivedDataProductVersionPublicationRequested.class);

        try {
            String dataProductId = extractDataProductId(dataProductVersionPublishEvent);
            PolicyResPolicyEvaluationRequest evaluationRequest = buildPolicyEvaluationRequestResource(dataProductVersionPublishEvent, dataProductId);
            PolicyResValidationResponse validationResponse = policyClient.validateInput(evaluationRequest, true);

            Object responseEvent = validationResponseToEvent(validationResponse, dataProductVersionPublishEvent, event.getSequenceId());
            notificationClient.notifyEvent(responseEvent);
        } catch (RuntimeException e) {
            // If the old parser fails during parsing in the policy v1 backward compatibility layer,
            // treat it as a validation failure and emit REJECTED event so the DPV validation state
            // is set to FAILED instead of remaining PENDING
            EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes sourceDataProductVersion =
                    dataProductVersionPublishEvent.getEventContent().getDataProductVersion();
            EventEmittedDataProductVersionPublicationRejected rejectEvent = buildRejectEvent(event.getSequenceId(), sourceDataProductVersion);
            notificationClient.notifyEvent(objectMapper.valueToTree(rejectEvent));
        }
    }


    private Object validationResponseToEvent(PolicyResValidationResponse validationResponse,
                                             EventReceivedDataProductVersionPublicationRequested originalEvent,
                                             Long sequenceId) {
        // Build typed event object based on validation result
        EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes sourceDataProductVersion =
                originalEvent.getEventContent().getDataProductVersion();

        // Check if any blocking policies failed
        boolean hasBlockingPolicyFailure = hasBlockingPolicyFailure(validationResponse);

        if (!hasBlockingPolicyFailure) {
            return objectMapper.valueToTree(buildApproveEvent(sequenceId, sourceDataProductVersion));
        } else {
            return objectMapper.valueToTree(buildRejectEvent(sequenceId, sourceDataProductVersion));
        }
    }

    private EventEmittedDataProductVersionPublicationRejected buildRejectEvent(Long sequenceId, EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes sourceDataProductVersion) {
        EventEmittedDataProductVersionPublicationRejected rejectedEvent = new EventEmittedDataProductVersionPublicationRejected();
        rejectedEvent.setSequenceId(sequenceId);
        rejectedEvent.setResourceIdentifier(sourceDataProductVersion.getUuid());

        EventEmittedDataProductVersionPublicationRejected.EventContent eventContent =
                new EventEmittedDataProductVersionPublicationRejected.EventContent();
        eventContent.setDataProductVersion(copyToRejectedDataProductVersion(sourceDataProductVersion));
        rejectedEvent.setEventContent(eventContent);

        return rejectedEvent;
    }

    private EventEmittedDataProductVersionPublicationApproved buildApproveEvent(Long sequenceId, EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes sourceDataProductVersion) {
        EventEmittedDataProductVersionPublicationApproved approvedEvent = new EventEmittedDataProductVersionPublicationApproved();
        approvedEvent.setSequenceId(sequenceId);
        approvedEvent.setResourceIdentifier(sourceDataProductVersion.getUuid());

        EventEmittedDataProductVersionPublicationApproved.EventContent eventContent =
                new EventEmittedDataProductVersionPublicationApproved.EventContent();
        eventContent.setDataProductVersion(copyToApprovedDataProductVersion(sourceDataProductVersion));
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
    /*
     *  This function mimics how the Registry V1 builds the event of DATA_PRODUCT_VERSION_CREATION
     *  To do this, it uses the old Data Product Descriptor parser which has some side effects during parsing,
     *  but they must be maintained to avoid breaking OPA policies built on top of its output
     * */

    private PolicyResPolicyEvaluationRequest buildPolicyEvaluationRequestResource(EventReceivedDataProductVersionPublicationRequested event, String dataProductId) {
        PolicyResPolicyEvaluationRequest evaluationRequest = new PolicyResPolicyEvaluationRequest();
        evaluationRequest.setEvent(PolicyResPolicyEvaluationRequest.EventType.DATA_PRODUCT_VERSION_CREATION);
        evaluationRequest.setResourceType(PolicyResPolicyEvaluationRequest.ResourceType.DATA_PRODUCT_DESCRIPTOR);
        evaluationRequest.setDataProductId(dataProductId);

        evaluationRequest.setDataProductVersion(event.getEventContent().getDataProductVersion().getTag());

        JsonNode newDescriptorJson = event.getEventContent().getDataProductVersion().getContent();
        JsonNode oldDescriptorJson = Optional.ofNullable(event.getEventContent().getPreviousDataProductVersion())
                .map(EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes::getContent)
                .orElse(null);

        DataProductVersionDPDS newDpds = parseDataProductVersionDPDS(newDescriptorJson);
        DataProductVersionDPDS oldDpds = oldDescriptorJson != null ? parseDataProductVersionDPDS(oldDescriptorJson) : null;

        JsonNode oldState = oldDpds != null ? objectMapper.valueToTree(new RegistryV1DataProductVersionEventState(oldDpds)) : null;
        JsonNode newState = objectMapper.valueToTree(new RegistryV1DataProductVersionEventState(newDpds));

        if (oldState != null) {
            fixDpdsVersionFieldName(oldState);
        }
        fixDpdsVersionFieldName(newState);

        evaluationRequest.setCurrentState(oldState);
        evaluationRequest.setAfterState(newState);

        return evaluationRequest;
    }

    private void fixDpdsVersionFieldName(JsonNode eventStateTree) {
        if (eventStateTree != null && eventStateTree.has("dataProductVersion")
                && eventStateTree.get("dataProductVersion").has("info")
                && eventStateTree.get("dataProductVersion").get("info").has("versionNumber")) {
            JsonNode versionNumberNode = eventStateTree.get("dataProductVersion").get("info").get("versionNumber");
            ((ObjectNode) eventStateTree.get("dataProductVersion").get("info")).remove("versionNumber");
            ((ObjectNode) eventStateTree.get("dataProductVersion").get("info")).set("version", versionNumberNode);
        }
    }

    private DataProductVersionDPDS parseDataProductVersionDPDS(JsonNode descriptorJson) {
        try {
            // Convert JsonNode to String
            String descriptorContent = objectMapper.writeValueAsString(descriptorJson);

            // Create DPDSParser
            DPDSParser descriptorParser = new DPDSParser(
                    "https://raw.githubusercontent.com/opendatamesh-initiative/odm-specification-dpdescriptor/main/schemas/",
                    "1.0.0",
                    "1.0.0"
            );

            // Create DescriptorLocation from content
            DescriptorLocation location = new UriLocation(descriptorContent);

            // Configure parse options
            ParseOptions options = new ParseOptions();
            options.setIdentifierStrategy(IdentifierStrategyFactory.getDefault());

            // Parse descriptor
            ParseResult result = descriptorParser.parse(location, options);
            return result.getDescriptorDocument();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert descriptor JSON to string", e);
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse data product version descriptor", e);
        }
    }

    private String extractDataProductId(EventReceivedDataProductVersionPublicationRequested event) {
        String dataProductId = event.getResourceIdentifier();
        if (dataProductId == null || dataProductId.isEmpty()) {
            throw new IllegalStateException("Missing resourceIdentifier in notification event");
        }
        return dataProductId;
    }

    private EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes copyToApprovedDataProductVersion(
            EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes source) {
        EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes target =
                new EventEmittedDataProductVersionPublicationApproved.DataProductVersionRes();
        target.setUuid(source.getUuid());
        target.setTag(source.getTag());

        if (source.getDataProduct() != null) {
            EventEmittedDataProductVersionPublicationApproved.DataProductRes targetDataProduct =
                    new EventEmittedDataProductVersionPublicationApproved.DataProductRes();
            targetDataProduct.setUuid(source.getDataProduct().getUuid());
            targetDataProduct.setFqn(source.getDataProduct().getFqn());
            target.setDataProduct(targetDataProduct);
        }

        return target;
    }

    private EventEmittedDataProductVersionPublicationRejected.DataProductVersionRes copyToRejectedDataProductVersion(
            EventReceivedDataProductVersionPublicationRequested.DataProductVersionRes source) {
        EventEmittedDataProductVersionPublicationRejected.DataProductVersionRes target =
                new EventEmittedDataProductVersionPublicationRejected.DataProductVersionRes();
        target.setUuid(source.getUuid());
        target.setTag(source.getTag());

        if (source.getDataProduct() != null) {
            EventEmittedDataProductVersionPublicationRejected.DataProductRes targetDataProduct =
                    new EventEmittedDataProductVersionPublicationRejected.DataProductRes();
            targetDataProduct.setUuid(source.getDataProduct().getUuid());
            targetDataProduct.setFqn(source.getDataProduct().getFqn());
            target.setDataProduct(targetDataProduct);
        }

        return target;
    }

    public static class RegistryV1DataProductVersionEventState {
        private DataProductVersionDPDS dataProductVersion;

        public RegistryV1DataProductVersionEventState() {
        }

        public RegistryV1DataProductVersionEventState(DataProductVersionDPDS dataProductVersion) {
            this.dataProductVersion = dataProductVersion;
        }

        public DataProductVersionDPDS getDataProductVersion() {
            return dataProductVersion;
        }

        public void setDataProductVersion(DataProductVersionDPDS dataProductVersion) {
            this.dataProductVersion = dataProductVersion;
        }
    }
}
