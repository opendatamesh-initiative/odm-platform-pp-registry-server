package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

class StoreDescriptorVariablePersistenceOutboundPortImpl implements StoreDescriptorVariablePersistenceOutboundPort {
    private final DescriptorVariableCrudService descriptorVariableCrudService;
    private final DataProductVersionCrudService dataProductVersionCrudService;

    StoreDescriptorVariablePersistenceOutboundPortImpl(DescriptorVariableCrudService descriptorVariableCrudService,
                                                       DataProductVersionCrudService dataProductVersionCrudService) {
        this.descriptorVariableCrudService = descriptorVariableCrudService;
        this.dataProductVersionCrudService = dataProductVersionCrudService;
    }

    @Override
    public DataProductVersion findDataProductVersionByUuid(String dataProductVersionUuid) {
        return dataProductVersionCrudService.findOne(dataProductVersionUuid);
    }

    @Override
    public DescriptorVariable createOrOverride(DescriptorVariable descriptorVariable) {
        DescriptorVariableSearchOptions searchOptions = new DescriptorVariableSearchOptions();
        searchOptions.setDataProductVersionUuid(descriptorVariable.getDataProductVersionUuid());
        searchOptions.setVariableKey(descriptorVariable.getVariableKey());

        Optional<DescriptorVariable> existingVariable = descriptorVariableCrudService.findAllFiltered(PageRequest.of(0, 1), searchOptions).stream().findFirst();

        if (existingVariable.isPresent()) {
            descriptorVariable.setSequenceId(existingVariable.get().getSequenceId());
            return descriptorVariableCrudService.overwrite(existingVariable.get().getSequenceId(), descriptorVariable);
        }
        return descriptorVariableCrudService.create(descriptorVariable);
    }
}
