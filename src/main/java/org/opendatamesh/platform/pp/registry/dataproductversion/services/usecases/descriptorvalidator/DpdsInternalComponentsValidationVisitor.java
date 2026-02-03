package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.internals.ApplicationComponent;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponent;
import org.opendatamesh.dpds.visitors.internals.InternalComponentsVisitor;

class DpdsInternalComponentsValidationVisitor implements InternalComponentsVisitor {

    private final DpdsDescriptorValidationContext context;
    private final DpdsVisitorState state;

    DpdsInternalComponentsValidationVisitor(DpdsDescriptorValidationContext context, DpdsVisitorState state) {
        this.context = context;
        this.state = state;
    }

    @Override
    public void visit(ApplicationComponent component) {
        if (component == null) return;

        String fieldPath = state.currentApplicationComponentFieldPath != null ? state.currentApplicationComponentFieldPath : "internalComponents.applicationComponents[]";

        DpdsValidationHelpers.validateRequiredStringField(component.getName(), fieldPath + ".name", context);
        if (component.getName() != null && !component.getName().isEmpty()) {
            context.addApplicationComponentName(component.getName(), fieldPath + ".name");
        }

        DpdsValidationHelpers.validateRequiredStringField(component.getVersion(), fieldPath + ".version", context);
        DpdsValidationHelpers.validateSemanticVersionIfPresent(component.getVersion(), fieldPath + ".version", context);
        context.addErrorIfInvalidEntityType(component.getEntityType(), "application", fieldPath + ".entityType");
    }

    @Override
    public void visit(InfrastructuralComponent component) {
        if (component == null) return;

        String fieldPath = state.currentInfrastructuralComponentFieldPath != null ? state.currentInfrastructuralComponentFieldPath : "internalComponents.infrastructuralComponents[]";

        DpdsValidationHelpers.validateRequiredStringField(component.getName(), fieldPath + ".name", context);
        if (component.getName() != null && !component.getName().isEmpty()) {
            context.addInfrastructuralComponentName(component.getName(), fieldPath + ".name");
        }

        DpdsValidationHelpers.validateRequiredStringField(component.getVersion(), fieldPath + ".version", context);
        DpdsValidationHelpers.validateSemanticVersionIfPresent(component.getVersion(), fieldPath + ".version", context);
        context.addErrorIfInvalidEntityType(component.getEntityType(), "infrastructure", fieldPath + ".entityType");
    }

    @Override
    public void visit(org.opendatamesh.dpds.model.internals.LifecycleTaskInfo lifecycleTaskInfo) {
        // Optional, no required fields
    }
}
