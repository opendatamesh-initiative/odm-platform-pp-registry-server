package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.core.StandardDefinition;
import org.opendatamesh.dpds.model.info.ContactPoint;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.model.interfaces.*;
import org.opendatamesh.dpds.model.internals.ApplicationComponent;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponent;
import org.opendatamesh.dpds.model.internals.InternalComponents;
import org.opendatamesh.dpds.model.internals.LifecycleTaskInfo;
import org.opendatamesh.dpds.visitors.DataProductVersionVisitor;
import org.opendatamesh.dpds.visitors.info.InfoVisitor;
import org.opendatamesh.dpds.visitors.interfaces.InterfaceComponentsVisitor;
import org.opendatamesh.dpds.visitors.interfaces.port.PortVisitor;
import org.opendatamesh.dpds.visitors.interfaces.port.PromisesVisitor;
import org.opendatamesh.dpds.visitors.internals.InternalComponentsVisitor;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Visitor that only fills in missing DPDS fields (entityType, id, fullyQualifiedName).
 * Assumes the descriptor has already been validated. Does not perform validation.
 */
class DpdsFieldGenerationVisitor implements DataProductVersionVisitor, InfoVisitor,
        InterfaceComponentsVisitor, PortVisitor, PromisesVisitor, InternalComponentsVisitor {

    private final DpdsFieldGenerator fieldGenerator = new DpdsFieldGenerator();
    private String dataProductFqn;

    private String currentPortType;
    private String currentPortFieldPath;
    private String currentStandardDefinitionContext;
    private String currentApplicationComponentFieldPath;
    private String currentInfrastructuralComponentFieldPath;

    @Override
    public void visit(Info info) {
        if (info == null) return;

        String fqn = info.getFullyQualifiedName();
        if (StringUtils.hasText(fqn)) {
            this.dataProductFqn = fqn;
        }

        if (!StringUtils.hasText(info.getEntityType())) {
            info.setEntityType("dataproduct");
        }
        if (!StringUtils.hasText(info.getId()) && StringUtils.hasText(fqn)) {
            info.setId(fieldGenerator.generateIdFromFqn(fqn));
        }

        if (info.getOwner() != null) {
            info.getOwner().accept(this);
        }
        if (info.getContactPoints() != null && !info.getContactPoints().isEmpty()) {
            for (int i = 0; i < info.getContactPoints().size(); i++) {
                ContactPoint contactPoint = info.getContactPoints().get(i);
                if (contactPoint != null) contactPoint.accept(this);
            }
        }
    }

    @Override
    public void visit(InterfaceComponents interfaceComponents) {
        if (interfaceComponents == null) return;

        if (interfaceComponents.getOutputPorts() != null) {
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
        if (interfaceComponents.getDiscoveryPorts() != null) {
            for (int i = 0; i < interfaceComponents.getDiscoveryPorts().size(); i++) {
                Port port = interfaceComponents.getDiscoveryPorts().get(i);
                if (port != null) {
                    currentPortType = "discoveryPort";
                    currentPortFieldPath = "interfaceComponents.discoveryPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }
        if (interfaceComponents.getObservabilityPorts() != null) {
            for (int i = 0; i < interfaceComponents.getObservabilityPorts().size(); i++) {
                Port port = interfaceComponents.getObservabilityPorts().get(i);
                if (port != null) {
                    currentPortType = "observabilityPort";
                    currentPortFieldPath = "interfaceComponents.observabilityPorts[" + i + "]";
                    port.accept(this);
                }
            }
        }
        if (interfaceComponents.getControlPorts() != null) {
            for (int i = 0; i < interfaceComponents.getControlPorts().size(); i++) {
                Port port = interfaceComponents.getControlPorts().get(i);
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
        if (internalComponents == null) return;
        if (internalComponents.getApplicationComponents() != null) {
            List<ApplicationComponent> list = internalComponents.getApplicationComponents();
            for (int i = 0; i < list.size(); i++) {
                ApplicationComponent component = list.get(i);
                if (component != null) {
                    currentApplicationComponentFieldPath = "internalComponents.applicationComponents[" + i + "]";
                    component.accept(this);
                }
            }
        }
        if (internalComponents.getInfrastructuralComponents() != null) {
            List<InfrastructuralComponent> list = internalComponents.getInfrastructuralComponents();
            for (int i = 0; i < list.size(); i++) {
                InfrastructuralComponent component = list.get(i);
                if (component != null) {
                    currentInfrastructuralComponentFieldPath = "internalComponents.infrastructuralComponents[" + i + "]";
                    component.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(Components components) {
        // No fields to generate for component maps
    }

    @Override
    public void visit(ExternalDocs externalDocs) {
    }

    @Override
    public void visit(Owner owner) {
    }

    @Override
    public void visit(ContactPoint contactPoint) {
    }

    @Override
    public void visit(Port port) {
        if (port == null) return;

        String name = port.getName();
        String expectedEntityType = fieldGenerator.getPortEntityType(currentPortType);
        if (expectedEntityType != null && !StringUtils.hasText(port.getEntityType())) {
            port.setEntityType(expectedEntityType);
        }

        String fqn = port.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(name) && currentPortType != null) {
            String fqnSegment = fieldGenerator.getPortFqnSegment(currentPortType);
            if (fqnSegment != null) {
                fqn = fieldGenerator.generateComponentFqn(dataProductFqn, fqnSegment, name);
                port.setFullyQualifiedName(fqn);
            }
        }
        if (!StringUtils.hasText(port.getId()) && StringUtils.hasText(fqn)) {
            port.setId(fieldGenerator.generateIdFromFqn(fqn));
        }

        if (port.getPromises() != null) port.getPromises().accept(this);
        if (port.getExpectations() != null) port.getExpectations().accept(this);
        if (port.getObligations() != null) port.getObligations().accept(this);
    }

    @Override
    public void visit(Promises promises) {
        if (promises == null) return;
        if (promises.getApi() != null) {
            currentStandardDefinitionContext = "api";
            promises.getApi().accept(this);
            currentStandardDefinitionContext = null;
        }
        if (promises.getDeprecationPolicy() != null) {
            currentStandardDefinitionContext = "api";
            promises.getDeprecationPolicy().accept(this);
            currentStandardDefinitionContext = null;
        }
        if (promises.getSlo() != null) {
            currentStandardDefinitionContext = "api";
            promises.getSlo().accept(this);
            currentStandardDefinitionContext = null;
        }
    }

    @Override
    public void visit(Expectations expectations) {
        if (expectations == null) return;
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
    public void visit(Obligations obligations) {
        if (obligations == null) return;
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

    @Override
    public void visit(StandardDefinition standardDefinition) {
        if (standardDefinition == null) return;

        String expectedEntityType = StringUtils.hasText(currentStandardDefinitionContext) ? currentStandardDefinitionContext : "api";
        if (!StringUtils.hasText(standardDefinition.getEntityType())) {
            standardDefinition.setEntityType(expectedEntityType);
        }

        String fqn = standardDefinition.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(standardDefinition.getName()) && StringUtils.hasText(standardDefinition.getVersion())) {
            try {
                String meshNamespace = fieldGenerator.extractMeshNamespaceFromInfoFqn(dataProductFqn);
                fqn = fieldGenerator.generateStandardDefinitionFqn(meshNamespace, expectedEntityType, standardDefinition.getName(), standardDefinition.getVersion());
                standardDefinition.setFullyQualifiedName(fqn);
            } catch (IllegalArgumentException ignored) {
                // Skip FQN generation if format is unexpected
            }
        }
        if (!StringUtils.hasText(standardDefinition.getId()) && StringUtils.hasText(fqn)) {
            standardDefinition.setId(fieldGenerator.generateIdFromFqn(fqn));
        }
    }

    @Override
    public void visit(ApplicationComponent component) {
        if (component == null) return;
        if (!StringUtils.hasText(component.getEntityType())) {
            component.setEntityType("application");
        }
        String fqn = component.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(component.getName())) {
            fqn = fieldGenerator.generateComponentFqn(dataProductFqn, "applications", component.getName());
            component.setFullyQualifiedName(fqn);
        }
        if (!StringUtils.hasText(component.getId()) && StringUtils.hasText(fqn)) {
            component.setId(fieldGenerator.generateIdFromFqn(fqn));
        }
    }

    @Override
    public void visit(InfrastructuralComponent component) {
        if (component == null) return;
        if (!StringUtils.hasText(component.getEntityType())) {
            component.setEntityType("infrastructure");
        }
        String fqn = component.getFullyQualifiedName();
        if (!StringUtils.hasText(fqn) && StringUtils.hasText(dataProductFqn) && StringUtils.hasText(component.getName())) {
            fqn = fieldGenerator.generateComponentFqn(dataProductFqn, "infrastructure", component.getName());
            component.setFullyQualifiedName(fqn);
        }
        if (!StringUtils.hasText(component.getId()) && StringUtils.hasText(fqn)) {
            component.setId(fieldGenerator.generateIdFromFqn(fqn));
        }
    }

    @Override
    public void visit(LifecycleTaskInfo lifecycleTaskInfo) {
    }
}
