package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

/**
 * Mutable state shared across DPDS validation sub-visitors during a single traversal.
 */
class DpdsVisitorState {
    String currentPortType;
    String currentPortFieldPath;
    String currentApplicationComponentFieldPath;
    String currentInfrastructuralComponentFieldPath;
    String currentStandardDefinitionContext;
    String currentStandardDefinitionPath;
}
