package org.opendatamesh.platform.pp.registry.descriptorvariable.services;

import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store.StoreDescriptorVariableCommand;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store.StoreDescriptorVariableFactory;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store.StoreDescriptorVariablePresenter;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DescriptorVariableUseCasesService {

    @Autowired
    private StoreDescriptorVariableFactory storeDescriptorVariableFactory;
    @Autowired
    private DescriptorVariableMapper mapper;

    public StoreDescriptorVariableResultRes storeDescriptorVariable(StoreDescriptorVariableCommandRes storeCommandRes) {
        // Convert list of resource DTOs to list of entities
        List<DescriptorVariable> descriptorVariables = new ArrayList<>();
        if (storeCommandRes.getDescriptorVariables() != null) {
            for (DescriptorVariableRes descriptorVariableRes : storeCommandRes.getDescriptorVariables()) {
                descriptorVariables.add(mapper.toEntity(descriptorVariableRes));
            }
        }

        StoreDescriptorVariableCommand storeCommand = new StoreDescriptorVariableCommand(descriptorVariables);

        DescriptorVariableResultHolder resultHolder = new DescriptorVariableResultHolder();

        storeDescriptorVariableFactory.buildStoreDescriptorVariable(
                storeCommand,
                resultHolder
        ).execute();

        // Convert list of entities back to list of resource DTOs
        List<DescriptorVariableRes> resultResList = new ArrayList<>();
        for (DescriptorVariable descriptorVariable : resultHolder.getResults()) {
            resultResList.add(mapper.toRes(descriptorVariable));
        }

        return new StoreDescriptorVariableResultRes(resultResList);
    }

    // Inner class to hold the result
    private static class DescriptorVariableResultHolder implements StoreDescriptorVariablePresenter {
        private final List<DescriptorVariable> results = new ArrayList<>();

        @Override
        public void presentDescriptorVariableStored(DescriptorVariable descriptorVariable) {
            this.results.add(descriptorVariable);
        }

        public List<DescriptorVariable> getResults() {
            return results;
        }
    }
}
