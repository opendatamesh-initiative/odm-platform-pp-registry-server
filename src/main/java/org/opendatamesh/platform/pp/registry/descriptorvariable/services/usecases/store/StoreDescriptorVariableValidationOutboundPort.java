package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

import java.util.List;

interface StoreDescriptorVariableValidationOutboundPort {
    /**
     * Validates that all descriptor variables belong to the specified data product version.
     * This is a domain-agnostic validation that checks the relationship.
     *
     * @param dataProductVersion the data product version entity
     * @param descriptorVariables the list of descriptor variables to validate
     * @throws org.opendatamesh.platform.pp.registry.exceptions.BadRequestException if any variable does not belong to the DPV
     */
    void validateVariablesCanBeAppliedToDescriptor(DataProductVersion dataProductVersion, List<DescriptorVariable> descriptorVariables);
}
