package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class StoreDescriptorVariable implements UseCase {
    private final StoreDescriptorVariableCommand command;
    private final StoreDescriptorVariablePresenter presenter;

    private final StoreDescriptorVariablePersistenceOutboundPort persistencePort;
    private final StoreDescriptorVariableValidationOutboundPort validationPort;
    private final TransactionalOutboundPort transactionalPort;

    StoreDescriptorVariable(StoreDescriptorVariableCommand command,
                            StoreDescriptorVariablePresenter presenter,
                            StoreDescriptorVariablePersistenceOutboundPort persistencePort,
                            StoreDescriptorVariableValidationOutboundPort validationPort,
                            TransactionalOutboundPort transactionalPort) {
        this.command = command;
        this.presenter = presenter;
        this.persistencePort = persistencePort;
        this.validationPort = validationPort;
        this.transactionalPort = transactionalPort;
    }

    @Override
    public void execute() {
        validateCommand(command);
        List<DescriptorVariable> descriptorVariables = command.descriptorVariables();
        Map<String, List<DescriptorVariable>> variablesByDpvUuid = groupVariablesByDataProductVersionUuid(descriptorVariables);

        for (Map.Entry<String, List<DescriptorVariable>> entry : variablesByDpvUuid.entrySet()) {
            String dataProductVersionUuid = entry.getKey();
            List<DescriptorVariable> groupVariables = entry.getValue();

            transactionalPort.doInTransaction(() -> {
                DataProductVersion dataProductVersion = persistencePort.findDataProductVersionByUuid(dataProductVersionUuid);
                validationPort.validateVariablesCanBeAppliedToDescriptor(dataProductVersion, groupVariables);

                for (DescriptorVariable descriptorVariable : groupVariables) {
                    DescriptorVariable saved = persistencePort.createOrOverride(descriptorVariable);
                    presenter.presentDescriptorVariableStored(saved);
                }
            });
        }
    }

    private Map<String, List<DescriptorVariable>> groupVariablesByDataProductVersionUuid(List<DescriptorVariable> descriptorVariables) {
        Map<String, List<DescriptorVariable>> grouped = new LinkedHashMap<>();
        for (DescriptorVariable descriptorVariable : descriptorVariables) {
            String dpvUuid = descriptorVariable.getDataProductVersionUuid();
            grouped.computeIfAbsent(dpvUuid, k -> new ArrayList<>()).add(descriptorVariable);
        }
        return grouped;
    }

    private void validateCommand(StoreDescriptorVariableCommand command) {
        if (command == null) {
            throw new BadRequestException("StoreDescriptorVariableCommand cannot be null");
        }

        if (command.descriptorVariables() == null || command.descriptorVariables().isEmpty()) {
            throw new BadRequestException("DescriptorVariables list cannot be null or empty");
        }

        // Validate each descriptor variable
        for (DescriptorVariable descriptorVariable : command.descriptorVariables()) {
            if (descriptorVariable == null) {
                throw new BadRequestException("DescriptorVariable in list cannot be null");
            }
            if (!StringUtils.hasText(descriptorVariable.getDataProductVersionUuid())) {
                throw new BadRequestException("Missing DataProductVersionUuid on DescriptorVariable");
            }
            if (!StringUtils.hasText(descriptorVariable.getVariableKey())) {
                throw new BadRequestException("Missing variable key on DescriptorVariable");
            }
        }
    }
}
