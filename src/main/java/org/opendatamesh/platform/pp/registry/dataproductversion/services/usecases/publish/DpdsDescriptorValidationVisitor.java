package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.core.StandardDefinition;
import org.opendatamesh.dpds.model.info.ContactPoint;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.model.interfaces.InterfaceComponents;
import org.opendatamesh.dpds.model.interfaces.Port;
import org.opendatamesh.dpds.model.interfaces.Promises;
import org.opendatamesh.dpds.model.internals.ApplicationComponent;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponent;
import org.opendatamesh.dpds.model.internals.InternalComponents;
import org.opendatamesh.dpds.visitors.DataProductVersionVisitor;
import org.opendatamesh.dpds.visitors.info.InfoVisitor;
import org.opendatamesh.dpds.visitors.interfaces.InterfaceComponentsVisitor;
import org.opendatamesh.dpds.visitors.interfaces.port.PortVisitor;
import org.opendatamesh.dpds.visitors.interfaces.port.PromisesVisitor;
import org.opendatamesh.dpds.visitors.internals.InternalComponentsVisitor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

class DpdsDescriptorValidationVisitor implements DataProductVersionVisitor, InfoVisitor, 
        InterfaceComponentsVisitor, PortVisitor, PromisesVisitor, InternalComponentsVisitor {
    private final SemanticVersionValidator semverValidator = new SemanticVersionValidator();
    private final DpdsDescriptorValidationContext context;
    private final DpdsFieldGenerator fieldGenerator;
    
    // Cached data product FQN (includes major version)
    private String dataProductFqn;
    
    // Track current context for proper field paths and uniqueness checking
    private String currentPortType;
    private String currentPortFieldPath;
    private String currentContactPointFieldPath;
    private String currentApplicationComponentFieldPath;
    private String currentInfrastructuralComponentFieldPath;
    private String currentStandardDefinitionContext; // "api" or "template"

    DpdsDescriptorValidationVisitor(DpdsDescriptorValidationContext context) {
        this.context = context;
        this.fieldGenerator = new DpdsFieldGenerator();
    }

    @Override
    public void visit(Info info) {
        if (info == null) {
            context.addError("info", "Info section is null");
            return;
        }

        // Validate fullyQualifiedName is present (cannot auto-generate for root)
        String fqn = info.getFullyQualifiedName();
        validateRequiredStringField(fqn, "info.fullyQualifiedName", context);
        
        // Cache the data product FQN for use in child components
        if (StringUtils.hasText(fqn)) {
            this.dataProductFqn = fqn;
        }
        
        // Validate/set entityType
        String entityType = info.getEntityType();
        if (!fieldGenerator.validateEntityType(entityType, "dataproduct", "info.entityType", context)) {
            // Entity type is either correct or missing - set it if missing
            if (!StringUtils.hasText(entityType)) {
                info.setEntityType("dataproduct");
            }
        }
        
        // Generate/set id if missing
        if (!StringUtils.hasText(info.getId()) && StringUtils.hasText(fqn)) {
            String generatedId = fieldGenerator.generateIdFromFqn(fqn);
            info.setId(generatedId);
        }

        validateRequiredStringField(info.getName(), "info.name", context);
        validateRequiredStringField(info.getVersion(), "info.version", context);
        validateRequiredStringField(info.getDomain(), "info.domain", context);

        if (info.getVersion() != null) {
            validateSemanticVersionIfPresent(info.getVersion(), "info.version", context);
        }

        if (info.getOwner() == null) {
            context.addError("info.owner", "Required field is missing");
        } else {
            info.getOwner().accept(this);
        }

        if (info.getContactPoints() != null && !info.getContactPoints().isEmpty()) {
            for (int i = 0; i < info.getContactPoints().size(); i++) {
                ContactPoint contactPoint = info.getContactPoints().get(i);
                if (contactPoint != null) {
                    currentContactPointFieldPath = "info.contactPoints[" + i + "]";
                    contactPoint.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(InterfaceComponents interfaceComponents) {
        if (interfaceComponents == null) {
            context.addError("interfaceComponents", "InterfaceComponents section is null");
            return;
        }

        // Validate that outputPorts field exists (list can be empty, but field must be present)
        if (interfaceComponents.getOutputPorts() == null) {
            context.addError("interfaceComponents.outputPorts", "OutputPorts field is required");
        } else {
            // Validate each output port
            List<Port> outputPorts = interfaceComponents.getOutputPorts();
            for (int i = 0; i < outputPorts.size(); i++) {
                Port port = outputPorts.get(i);
                if (port != null) {
                    currentPortType = "outputPort";
                    currentPortFieldPath = "interfaceComponents.outputPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }

        // Validate inputPorts if present
        if (interfaceComponents.getInputPorts() != null) {
            List<Port> inputPorts = interfaceComponents.getInputPorts();
            for (int i = 0; i < inputPorts.size(); i++) {
                Port port = inputPorts.get(i);
                if (port != null) {
                    currentPortType = "inputPort";
                    currentPortFieldPath = "interfaceComponents.inputPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }

        // Validate discoveryPorts if present
        if (interfaceComponents.getDiscoveryPorts() != null) {
            List<Port> discoveryPorts = interfaceComponents.getDiscoveryPorts();
            for (int i = 0; i < discoveryPorts.size(); i++) {
                Port port = discoveryPorts.get(i);
                if (port != null) {
                    currentPortType = "discoveryPort";
                    currentPortFieldPath = "interfaceComponents.discoveryPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }

        // Validate observabilityPorts if present
        if (interfaceComponents.getObservabilityPorts() != null) {
            List<Port> observabilityPorts = interfaceComponents.getObservabilityPorts();
            for (int i = 0; i < observabilityPorts.size(); i++) {
                Port port = observabilityPorts.get(i);
                if (port != null) {
                    currentPortType = "observabilityPort";
                    currentPortFieldPath = "interfaceComponents.observabilityPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }

        // Validate controlPorts if present
        if (interfaceComponents.getControlPorts() != null) {
            List<Port> controlPorts = interfaceComponents.getControlPorts();
            for (int i = 0; i < controlPorts.size(); i++) {
                Port port = controlPorts.get(i);
                if (port != null) {
                    currentPortType = "controlPort";
                    currentPortFieldPath = "interfaceComponents.controlPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(InternalComponents internalComponents) {
        if (internalComponents == null) {
            return; // InternalComponents is optional
        }

        // Validate applicationComponents if present
        if (internalComponents.getApplicationComponents() != null) {
            List<ApplicationComponent> appComponents = internalComponents.getApplicationComponents();
            for (int i = 0; i < appComponents.size(); i++) {
                ApplicationComponent component = appComponents.get(i);
                if (component != null) {
                    currentApplicationComponentFieldPath = "internalComponents.applicationComponents[" + i + "]";
                    component.accept(this);
                }
            }
        }

        // Validate infrastructuralComponents if present
        if (internalComponents.getInfrastructuralComponents() != null) {
            List<InfrastructuralComponent> infraComponents = internalComponents.getInfrastructuralComponents();
            for (int i = 0; i < infraComponents.size(); i++) {
                InfrastructuralComponent component = infraComponents.get(i);
                if (component != null) {
                    currentInfrastructuralComponentFieldPath = "internalComponents.infrastructuralComponents[" + i + "]";
                    component.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(Components components) {
        if (components == null) {
            return; // Components is optional
        }

        // Validate all component maps for key format and uniqueness
        validateComponentMap(components.getInputPorts(), "components.inputPorts");
        validateComponentMap(components.getOutputPorts(), "components.outputPorts");
        validateComponentMap(components.getDiscoveryPorts(), "components.discoveryPorts");
        validateComponentMap(components.getObservabilityPorts(), "components.observabilityPorts");
        validateComponentMap(components.getControlPorts(), "components.controlPorts");
        validateComponentMap(components.getApplicationComponents(), "components.applicationComponents");
        validateComponentMap(components.getInfrastructuralComponents(), "components.infrastructuralComponents");
        validateComponentMap(components.getApis(), "components.apis");
        validateComponentMap(components.getTemplates(), "components.templates");
    }

    @Override
    public void visit(ExternalDocs externalDocs) {
        // ExternalDocs is optional and has no required fields
    }

    @Override
    public void visit(Owner owner) {
        if (owner == null) {
            context.addError("info.owner", "Owner section is null");
            return;
        }

        // Validate required field in owner
        validateRequiredStringField(owner.getId(), "info.owner.id", context);
    }

    @Override
    public void visit(ContactPoint contactPoint) {
        // ContactPoint is optional and has no required fields  
    }

    // InterfaceComponentsVisitor implementation
    @Override
    public void visit(Port port) {
        if (port == null) {
            return;
        }

        String fieldPath = currentPortFieldPath != null ? currentPortFieldPath : "interfaceComponents.port";
        
        // Validate required fields
        String name = port.getName();
        validateRequiredStringField(name, fieldPath + ".name", context);
        if (name != null && !name.isEmpty() && currentPortType != null) {
            // Track name for uniqueness based on port type
            switch (currentPortType) {
                case "inputPort":
                    context.addInputPortName(name, fieldPath + ".name");
                    break;
                case "outputPort":
                    context.addOutputPortName(name, fieldPath + ".name");
                    break;
                case "discoveryPort":
                    context.addDiscoveryPortName(name, fieldPath + ".name");
                    break;
                case "observabilityPort":
                    context.addObservabilityPortName(name, fieldPath + ".name");
                    break;
                case "controlPort":
                    context.addControlPortName(name, fieldPath + ".name");
                    break;
            }
        }

        String version = port.getVersion();
        validateRequiredStringField(version, fieldPath + ".version", context);
        if (version != null && !version.isEmpty()) {
            validateSemanticVersionIfPresent(version, fieldPath + ".version", context);
        }
        
        // Validate/set entityType based on port type
        if (currentPortType != null) {
            String expectedEntityType = fieldGenerator.getPortEntityType(currentPortType);
            if (expectedEntityType != null) {
                String actualEntityType = port.getEntityType();
                if (!fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, 
                        fieldPath + ".entityType", context)) {
                    // Entity type is either correct or missing - set it if missing
                    if (!StringUtils.hasText(actualEntityType)) {
                        port.setEntityType(expectedEntityType);
                    }
                }
            }
        }
        
        // Generate/set fullyQualifiedName if missing
        String fqn = port.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && 
                StringUtils.hasText(name) && currentPortType != null) {
            String fqnSegment = fieldGenerator.getPortFqnSegment(currentPortType);
            if (fqnSegment != null) {
                String generatedFqn = fieldGenerator.generateComponentFqn(dataProductFqn, fqnSegment, name);
                port.setFullyQualifiedName(generatedFqn);
                fqn = generatedFqn; // Update for ID generation
            }
        }
        
        // Generate/set id if missing (after FQN is available)
        if (!StringUtils.hasText(port.getId()) && StringUtils.hasText(fqn)) {
            String generatedId = fieldGenerator.generateIdFromFqn(fqn);
            port.setId(generatedId);
        }

        // Validate promises if present (for output ports)
        if (port.getPromises() != null) {
            port.getPromises().accept(this);
        }
        
        // Validate expectations if present
        if (port.getExpectations() != null) {
            port.getExpectations().accept(this);
        }
        
        // Validate obligations if present
        if (port.getObligations() != null) {
            port.getObligations().accept(this);
        }
    }

    // PortVisitor implementation
    @Override
    public void visit(Promises promises) {
        if (promises == null) {
            return;
        }

        if (promises.getApi() != null) {
            currentStandardDefinitionContext = "api";
            promises.getApi().accept(this);
            currentStandardDefinitionContext = null;
        }
        if (promises.getDeprecationPolicy() != null) {
            currentStandardDefinitionContext = "api"; // deprecationPolicy is also an API-like standard
            promises.getDeprecationPolicy().accept(this);
            currentStandardDefinitionContext = null;
        }
        if (promises.getSlo() != null) {
            currentStandardDefinitionContext = "api"; // slo is also an API-like standard
            promises.getSlo().accept(this);
            currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.interfaces.Expectations expectations) {
        if (expectations == null) {
            return;
        }
        
        // Visit StandardDefinition children with "api" context
        if (expectations.getAudience() != null) {
            currentStandardDefinitionContext = "api";
            expectations.getAudience().accept(this);
            currentStandardDefinitionContext = null;
        }
        
        if (expectations.getUsage() != null) {
            currentStandardDefinitionContext = "api";
            expectations.getUsage().accept(this);
            currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.interfaces.Obligations obligations) {
        if (obligations == null) {
            return;
        }
        
        // Visit StandardDefinition children with "api" context
        if (obligations.getTermsAndConditions() != null) {
            currentStandardDefinitionContext = "api";
            obligations.getTermsAndConditions().accept(this);
            currentStandardDefinitionContext = null;
        }
        
        if (obligations.getBillingPolicy() != null) {
            currentStandardDefinitionContext = "api";
            obligations.getBillingPolicy().accept(this);
            currentStandardDefinitionContext = null;
        }
        
        if (obligations.getSla() != null) {
            currentStandardDefinitionContext = "api";
            obligations.getSla().accept(this);
            currentStandardDefinitionContext = null;
        }
    }

    // PromisesVisitor implementation
    @Override
    public void visit(StandardDefinition standardDefinition) {
        if (standardDefinition == null) {
            return;
        }

        // Determine field path
        String fieldPath = currentPortFieldPath != null ? currentPortFieldPath + ".api" : "standardDefinition";

        String name = standardDefinition.getName();
        validateRequiredStringField(name, fieldPath + ".name", context);
        
        String version = standardDefinition.getVersion();
        validateRequiredStringField(version, fieldPath + ".version", context);
        
        validateRequiredStringField(standardDefinition.getSpecification(), fieldPath + ".specification", context);

        // Validate definition is present (must contain either $href or inline content)
        if (standardDefinition.getDefinition() == null) {
            context.addError(fieldPath + ".definition", "Required field is missing");
        }
        
        // Validate/set entityType based on context (api or template)
        String expectedEntityType = StringUtils.hasText(currentStandardDefinitionContext) 
            ? currentStandardDefinitionContext 
            : "api";
        
        String entityType = standardDefinition.getEntityType();
        if (!fieldGenerator.validateEntityType(entityType, expectedEntityType, fieldPath + ".entityType", context)) {
            // Entity type is either correct or missing - set it if missing
            if (!StringUtils.hasText(entityType)) {
                standardDefinition.setEntityType(expectedEntityType);
            }
        }
        
        // Generate/set fullyQualifiedName if missing
        // Format: urn:dpds:{mesh-namespace}:{entity-type}s:{name}:{version}
        String fqn = standardDefinition.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && 
                StringUtils.hasText(name) && StringUtils.hasText(version)) {
            try {
                String meshNamespace = fieldGenerator.extractMeshNamespaceFromInfoFqn(dataProductFqn);
                String generatedFqn = fieldGenerator.generateStandardDefinitionFqn(
                    meshNamespace, 
                    expectedEntityType, 
                    name, 
                    version
                );
                standardDefinition.setFullyQualifiedName(generatedFqn);
                fqn = generatedFqn; // Update for ID generation
            } catch (IllegalArgumentException e) {
                context.addError(fieldPath + ".fullyQualifiedName", 
                    "Cannot generate FQN: " + e.getMessage());
            }
        }
        
        // Generate/set id if missing (after FQN is available)
        if (!StringUtils.hasText(standardDefinition.getId()) && StringUtils.hasText(fqn)) {
            String generatedId = fieldGenerator.generateIdFromFqn(fqn);
            standardDefinition.setId(generatedId);
        }
    }

    // InternalComponentsVisitor implementation
    @Override
    public void visit(ApplicationComponent component) {
        if (component == null) {
            return;
        }

        String fieldPath = currentApplicationComponentFieldPath != null ? currentApplicationComponentFieldPath : "internalComponents.applicationComponents[]";

        // Validate required fields
        String name = component.getName();
        validateRequiredStringField(name, fieldPath + ".name", context);
        if (name != null && !name.isEmpty()) {
            context.addApplicationComponentName(name, fieldPath + ".name");
        }

        String version = component.getVersion();
        validateRequiredStringField(version, fieldPath + ".version", context);
        if (version != null && !version.isEmpty()) {
            validateSemanticVersionIfPresent(version, fieldPath + ".version", context);
        }
        
        // Validate/set entityType
        String entityType = component.getEntityType();
        if (!fieldGenerator.validateEntityType(entityType, "application", fieldPath + ".entityType", context)) {
            // Entity type is either correct or missing - set it if missing
            if (!StringUtils.hasText(entityType)) {
                component.setEntityType("application");
            }
        }
        
        // Generate/set fullyQualifiedName if missing
        String fqn = component.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(name)) {
            String generatedFqn = fieldGenerator.generateComponentFqn(dataProductFqn, "applications", name);
            component.setFullyQualifiedName(generatedFqn);
            fqn = generatedFqn; // Update for ID generation
        }
        
        // Generate/set id if missing (after FQN is available)
        if (!StringUtils.hasText(component.getId()) && StringUtils.hasText(fqn)) {
            String generatedId = fieldGenerator.generateIdFromFqn(fqn);
            component.setId(generatedId);
        }
    }

    @Override
    public void visit(InfrastructuralComponent component) {
        if (component == null) {
            return;
        }

        String fieldPath = currentInfrastructuralComponentFieldPath != null ? currentInfrastructuralComponentFieldPath : "internalComponents.infrastructuralComponents[]";

        // Validate required fields
        String name = component.getName();
        validateRequiredStringField(name, fieldPath + ".name", context);
        if (name != null && !name.isEmpty()) {
            context.addInfrastructuralComponentName(name, fieldPath + ".name");
        }

        String version = component.getVersion();
        validateRequiredStringField(version, fieldPath + ".version", context);
        if (version != null && !version.isEmpty()) {
            validateSemanticVersionIfPresent(version, fieldPath + ".version", context);
        }
        
        // Validate/set entityType
        String entityType = component.getEntityType();
        if (!fieldGenerator.validateEntityType(entityType, "infrastructure", fieldPath + ".entityType", context)) {
            // Entity type is either correct or missing - set it if missing
            if (!StringUtils.hasText(entityType)) {
                component.setEntityType("infrastructure");
            }
        }
        
        // Generate/set fullyQualifiedName if missing
        String fqn = component.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(name)) {
            String generatedFqn = fieldGenerator.generateComponentFqn(dataProductFqn, "infrastructure", name);
            component.setFullyQualifiedName(generatedFqn);
            fqn = generatedFqn; // Update for ID generation
        }
        
        // Generate/set id if missing (after FQN is available)
        if (!StringUtils.hasText(component.getId()) && StringUtils.hasText(fqn)) {
            String generatedId = fieldGenerator.generateIdFromFqn(fqn);
            component.setId(generatedId);
        }
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.internals.LifecycleTaskInfo lifecycleTaskInfo) {
        // LifecycleTaskInfo is optional and has no required fields for validation
    }

    // Helper method to validate component maps
    private void validateComponentMap(Map<String, ?> componentMap, String mapType) {
        if (componentMap == null) {
            return;
        }

        for (String key : componentMap.keySet()) {
            context.addComponentKey(mapType, key, mapType + "[\"" + key + "\"]");
        }
    }

    private void validateRequiredStringField(String value, String fieldPath, DpdsDescriptorValidationContext context) {
        if (!StringUtils.hasText(value)) {
            context.addError(fieldPath, "Required field is missing or empty");
        }
    }

    private void validateSemanticVersionIfPresent(String version, String fieldPath, DpdsDescriptorValidationContext context) {
        if (StringUtils.hasText(version) && !semverValidator.isValid(version)) {
            context.addError(fieldPath, String.format("Version '%s' does not follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD])", version));
        }
    }
}
