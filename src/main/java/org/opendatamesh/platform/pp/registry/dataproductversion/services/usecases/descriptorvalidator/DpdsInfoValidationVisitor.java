package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.info.ContactPoint;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.visitors.info.InfoVisitor;

class DpdsInfoValidationVisitor implements InfoVisitor {

    private final DpdsDescriptorValidationContext context;

    DpdsInfoValidationVisitor(DpdsDescriptorValidationContext context) {
        this.context = context;
    }

    @Override
    public void visit(Owner owner) {
        if (owner == null) {
            context.addError("info.owner", "Owner section is null");
            return;
        }
        DpdsValidationHelpers.validateRequiredStringField(owner.getId(), "info.owner.id", context);
    }

    @Override
    public void visit(ContactPoint contactPoint) {
        // ContactPoint is optional, no required fields
    }
}
