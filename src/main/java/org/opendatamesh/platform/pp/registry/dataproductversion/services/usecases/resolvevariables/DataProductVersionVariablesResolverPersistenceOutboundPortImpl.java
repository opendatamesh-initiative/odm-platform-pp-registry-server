package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

class DataProductVersionVariablesResolverPersistenceOutboundPortImpl implements DataProductVersionVariablesResolverPersistenceOutboundPort {
    private final DataProductVersionCrudService dataProductVersionCrudService;
    private final DescriptorVariableCrudService descriptorVariableCrudService;

    DataProductVersionVariablesResolverPersistenceOutboundPortImpl(DataProductVersionCrudService dataProductVersionCrudService,
                                                                   DescriptorVariableCrudService descriptorVariableCrudService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
        this.descriptorVariableCrudService = descriptorVariableCrudService;
    }

    @Override
    public DataProductVersion findByUuid(String dataProductVersionUuid) {
        return dataProductVersionCrudService.findOne(dataProductVersionUuid);
    }

    @Override
    public List<DescriptorVariable> findDescriptorVariables(String dataProductVersionUuid) {
        DescriptorVariableSearchOptions searchOptions = new DescriptorVariableSearchOptions();
        searchOptions.setDataProductVersionUuid(dataProductVersionUuid);
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable> page = 
            descriptorVariableCrudService.findAllFiltered(pageable, searchOptions);
        
        return page.getContent();
    }
}
