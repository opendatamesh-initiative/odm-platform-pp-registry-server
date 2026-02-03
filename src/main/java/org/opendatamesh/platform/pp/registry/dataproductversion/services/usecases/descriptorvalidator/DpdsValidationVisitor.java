package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.ExternalDocs;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.interfaces.InterfaceComponents;
import org.opendatamesh.dpds.model.interfaces.Port;
import org.opendatamesh.dpds.model.internals.InternalComponents;
import org.opendatamesh.dpds.visitors.DataProductVersionVisitor;

import java.util.List;

class DpdsValidationVisitor implements DataProductVersionVisitor {

    private final DpdsDescriptorValidationContext context;
    private final DpdsVisitorState state;
    private final DpdsInfoValidationVisitor infoVisitor;
    private final DpdsPortValidationVisitor portVisitor;
    private final DpdsInterfaceComponentsValidationVisitor interfaceComponentsVisitor;
    private final DpdsInternalComponentsValidationVisitor internalComponentsVisitor;
    private final DpdsComponentsValidationVisitor componentsVisitor;

    DpdsValidationVisitor(DpdsDescriptorValidationContext context) {
        this.context = context;
        this.state = new DpdsVisitorState();
        this.infoVisitor = new DpdsInfoValidationVisitor(context);
        this.portVisitor = new DpdsPortValidationVisitor(context, state);
        this.interfaceComponentsVisitor = new DpdsInterfaceComponentsValidationVisitor(context, state, portVisitor);
        this.internalComponentsVisitor = new DpdsInternalComponentsValidationVisitor(context, state);
        this.componentsVisitor = new DpdsComponentsValidationVisitor(context);
    }

    @Override
    public void visit(Info info) {
        if (info == null) return;

        DpdsValidationHelpers.validateRequiredStringField(info.getName(), "info.name", context);
        DpdsValidationHelpers.validateRequiredStringField(info.getVersion(), "info.version", context);
        DpdsValidationHelpers.validateSemanticVersionIfPresent(info.getVersion(), "info.version", context);
        DpdsValidationHelpers.validateRequiredStringField(info.getDomain(), "info.domain", context);
        DpdsValidationHelpers.validateRequiredStringField(info.getFullyQualifiedName(), "info.fullyQualifiedName", context);

        if (info.getOwner() == null) {
            context.addError("info.owner", "Owner section is required");
        } else {
            info.getOwner().accept(infoVisitor);
        }

        if (info.getContactPoints() != null) {
            for (org.opendatamesh.dpds.model.info.ContactPoint contactPoint : info.getContactPoints()) {
                if (contactPoint != null) {
                    contactPoint.accept(infoVisitor);
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

        if (interfaceComponents.getOutputPorts() == null) {
            context.addError("interfaceComponents.outputPorts", "OutputPorts field is required");
        } else {
            List<Port> outputPorts = interfaceComponents.getOutputPorts();
            for (int i = 0; i < outputPorts.size(); i++) {
                Port port = outputPorts.get(i);
                if (port != null) {
                    state.currentPortType = "outputPort";
                    state.currentPortFieldPath = "interfaceComponents.outputPorts[" + i + "]";
                    interfaceComponentsVisitor.visit(port);
                }
            }
        }

        if (interfaceComponents.getInputPorts() != null) {
            List<Port> inputPorts = interfaceComponents.getInputPorts();
            for (int i = 0; i < inputPorts.size(); i++) {
                Port port = inputPorts.get(i);
                if (port != null) {
                    state.currentPortType = "inputPort";
                    state.currentPortFieldPath = "interfaceComponents.inputPorts[" + i + "]";
                    interfaceComponentsVisitor.visit(port);
                }
            }
        }

        if (interfaceComponents.getDiscoveryPorts() != null) {
            List<Port> discoveryPorts = interfaceComponents.getDiscoveryPorts();
            for (int i = 0; i < discoveryPorts.size(); i++) {
                Port port = discoveryPorts.get(i);
                if (port != null) {
                    state.currentPortType = "discoveryPort";
                    state.currentPortFieldPath = "interfaceComponents.discoveryPorts[" + i + "]";
                    interfaceComponentsVisitor.visit(port);
                }
            }
        }

        if (interfaceComponents.getObservabilityPorts() != null) {
            List<Port> observabilityPorts = interfaceComponents.getObservabilityPorts();
            for (int i = 0; i < observabilityPorts.size(); i++) {
                Port port = observabilityPorts.get(i);
                if (port != null) {
                    state.currentPortType = "observabilityPort";
                    state.currentPortFieldPath = "interfaceComponents.observabilityPorts[" + i + "]";
                    interfaceComponentsVisitor.visit(port);
                }
            }
        }

        if (interfaceComponents.getControlPorts() != null) {
            List<Port> controlPorts = interfaceComponents.getControlPorts();
            for (int i = 0; i < controlPorts.size(); i++) {
                Port port = controlPorts.get(i);
                if (port != null) {
                    state.currentPortType = "controlPort";
                    state.currentPortFieldPath = "interfaceComponents.controlPorts[" + i + "]";
                    interfaceComponentsVisitor.visit(port);
                }
            }
        }
    }

    @Override
    public void visit(InternalComponents internalComponents) {
        if (internalComponents == null) return;

        if (internalComponents.getApplicationComponents() != null) {
            List<org.opendatamesh.dpds.model.internals.ApplicationComponent> appComponents = internalComponents.getApplicationComponents();
            for (int i = 0; i < appComponents.size(); i++) {
                org.opendatamesh.dpds.model.internals.ApplicationComponent component = appComponents.get(i);
                if (component != null) {
                    state.currentApplicationComponentFieldPath = "internalComponents.applicationComponents[" + i + "]";
                    component.accept(internalComponentsVisitor);
                }
            }
        }

        if (internalComponents.getInfrastructuralComponents() != null) {
            List<org.opendatamesh.dpds.model.internals.InfrastructuralComponent> infraComponents = internalComponents.getInfrastructuralComponents();
            for (int i = 0; i < infraComponents.size(); i++) {
                org.opendatamesh.dpds.model.internals.InfrastructuralComponent component = infraComponents.get(i);
                if (component != null) {
                    state.currentInfrastructuralComponentFieldPath = "internalComponents.infrastructuralComponents[" + i + "]";
                    component.accept(internalComponentsVisitor);
                }
            }
        }
    }

    @Override
    public void visit(Components components) {
        components.accept(componentsVisitor);
    }

    @Override
    public void visit(ExternalDocs externalDocs) {
        // Optional, no required fields
    }
}
