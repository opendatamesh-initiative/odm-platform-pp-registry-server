package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.core.StandardDefinition;
import org.opendatamesh.dpds.model.interfaces.Port;
import org.opendatamesh.dpds.model.interfaces.Promises;
import org.opendatamesh.dpds.visitors.interfaces.port.PortVisitor;
import org.opendatamesh.dpds.visitors.interfaces.port.PromisesVisitor;
import org.springframework.util.StringUtils;

class DpdsPortValidationVisitor implements PortVisitor, PromisesVisitor {

    private final DpdsDescriptorValidationContext context;
    private final DpdsVisitorState state;

    DpdsPortValidationVisitor(DpdsDescriptorValidationContext context, DpdsVisitorState state) {
        this.context = context;
        this.state = state;
    }

    /**
     * Validates port-level fields (name, version, entityType) and then visits promises/expectations/obligations.
     * Called from DpdsInterfaceComponentsValidationVisitor.visit(Port) because Port does not accept PortVisitor.
     */
    void validatePort(Port port) {
        if (port == null) return;

        String fieldPath = state.currentPortFieldPath != null ? state.currentPortFieldPath : "interfaceComponents.port";

        String name = port.getName();
        DpdsValidationHelpers.validateRequiredStringField(name, fieldPath + ".name", context);
        if (name != null && !name.isEmpty() && state.currentPortType != null) {
            switch (state.currentPortType) {
                case "inputPort" -> context.addInputPortName(name, fieldPath + ".name");
                case "outputPort" -> context.addOutputPortName(name, fieldPath + ".name");
                case "discoveryPort" -> context.addDiscoveryPortName(name, fieldPath + ".name");
                case "observabilityPort" -> context.addObservabilityPortName(name, fieldPath + ".name");
                case "controlPort" -> context.addControlPortName(name, fieldPath + ".name");
                default -> { }
            }
        }

        String version = port.getVersion();
        DpdsValidationHelpers.validateRequiredStringField(version, fieldPath + ".version", context);
        DpdsValidationHelpers.validateSemanticVersionIfPresent(version, fieldPath + ".version", context);

        String expectedEntityType = DpdsValidationHelpers.getPortEntityType(state.currentPortType);
        if (expectedEntityType != null) {
            context.addErrorIfInvalidEntityType(port.getEntityType(), expectedEntityType, fieldPath + ".entityType");
        }

        if (port.getPromises() != null) port.getPromises().accept(this);
        if (port.getExpectations() != null) port.getExpectations().accept(this);
        if (port.getObligations() != null) port.getObligations().accept(this);
    }

    @Override
    public void visit(ExternalDocs externalDocs) {
        // Optional, no required fields
    }

    @Override
    public void visit(Promises promises) {
        if (promises == null) return;
        if (promises.getApi() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".promises.api" : "promises.api";
            promises.getApi().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
        if (promises.getDeprecationPolicy() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".promises.deprecationPolicy" : "promises.deprecationPolicy";
            promises.getDeprecationPolicy().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
        if (promises.getSlo() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".promises.slo" : "promises.slo";
            promises.getSlo().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.interfaces.Expectations expectations) {
        if (expectations == null) return;
        if (expectations.getAudience() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".expectations.audience" : "expectations.audience";
            expectations.getAudience().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
        if (expectations.getUsage() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".expectations.usage" : "expectations.usage";
            expectations.getUsage().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.interfaces.Obligations obligations) {
        if (obligations == null) return;
        if (obligations.getTermsAndConditions() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".obligations.termsAndConditions" : "obligations.termsAndConditions";
            obligations.getTermsAndConditions().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
        if (obligations.getBillingPolicy() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".obligations.billingPolicy" : "obligations.billingPolicy";
            obligations.getBillingPolicy().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
        if (obligations.getSla() != null) {
            state.currentStandardDefinitionContext = "api";
            state.currentStandardDefinitionPath = state.currentPortFieldPath != null ? state.currentPortFieldPath + ".obligations.sla" : "obligations.sla";
            obligations.getSla().accept(this);
            state.currentStandardDefinitionPath = null;
            state.currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(StandardDefinition standardDefinition) {
        if (standardDefinition == null) return;

        String fieldPath = state.currentStandardDefinitionPath != null ? state.currentStandardDefinitionPath
                : (state.currentPortFieldPath != null ? state.currentPortFieldPath + ".api" : "standardDefinition");

        DpdsValidationHelpers.validateRequiredStringField(standardDefinition.getName(), fieldPath + ".name", context);
        DpdsValidationHelpers.validateRequiredStringField(standardDefinition.getVersion(), fieldPath + ".version", context);
        DpdsValidationHelpers.validateSemanticVersionIfPresent(standardDefinition.getVersion(), fieldPath + ".version", context);
        DpdsValidationHelpers.validateRequiredStringField(standardDefinition.getSpecification(), fieldPath + ".specification", context);

        if (standardDefinition.getDefinition() == null) {
            context.addError(fieldPath + ".definition", "Required field is missing (must contain $href or inline content)");
        }

        String expectedEntityType = StringUtils.hasText(state.currentStandardDefinitionContext) ? state.currentStandardDefinitionContext : "api";
        context.addErrorIfInvalidEntityType(standardDefinition.getEntityType(), expectedEntityType, fieldPath + ".entityType");
    }
}
