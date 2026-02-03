package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.visitors.DataProductVersionVisitor;

import java.util.Map;

class DpdsComponentsValidationVisitor implements DataProductVersionVisitor {

    private final DpdsDescriptorValidationContext context;

    DpdsComponentsValidationVisitor(DpdsDescriptorValidationContext context) {
        this.context = context;
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.info.Info info) {
        // Only visit(Components) is implemented
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.interfaces.InterfaceComponents interfaceComponents) {
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.internals.InternalComponents internalComponents) {
    }

    @Override
    public void visit(Components components) {
        if (components == null) return;

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
    public void visit(org.opendatamesh.dpds.model.core.ExternalDocs externalDocs) {
    }

    private void validateComponentMap(Map<String, ?> componentMap, String mapType) {
        if (componentMap == null) return;
        for (String key : componentMap.keySet()) {
            context.addComponentKey(mapType, key, mapType + "[\"" + key + "\"]");
        }
    }
}
