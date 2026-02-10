package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.opendatamesh.dpds.model.interfaces.Port;
import org.opendatamesh.dpds.visitors.interfaces.InterfaceComponentsVisitor;

/**
 * Implements InterfaceComponentsVisitor (visit(Port) only).
 * Delegates port validation to DpdsPortValidationVisitor.validatePort and sub-visits via accept(PortVisitor).
 */
class DpdsInterfaceComponentsValidationVisitor implements InterfaceComponentsVisitor {

    private final DpdsDescriptorValidationContext context;
    private final DpdsVisitorState state;
    private final DpdsPortValidationVisitor portVisitor;

    DpdsInterfaceComponentsValidationVisitor(DpdsDescriptorValidationContext context, DpdsVisitorState state,
                                             DpdsPortValidationVisitor portVisitor) {
        this.context = context;
        this.state = state;
        this.portVisitor = portVisitor;
    }

    @Override
    public void visit(Port port) {
        if (port == null) return;
        portVisitor.validatePort(port);
    }
}
