package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.utils.usecases.UseCase;
import org.springframework.util.StringUtils;

import java.util.List;

class DataProductVersionVariablesResolver implements UseCase {
    private final DataProductVersionVariablesResolverCommand command;
    private final DataProductVersionVariablesResolverPresenter presenter;

    private final DataProductVersionVariablesResolverPersistenceOutboundPort persistencePort;
    private final DataProductVersionVariablesResolverDescriptorOutboundPort descriptorPort;

    DataProductVersionVariablesResolver(DataProductVersionVariablesResolverCommand command,
                                        DataProductVersionVariablesResolverPresenter presenter,
                                        DataProductVersionVariablesResolverPersistenceOutboundPort persistencePort, DataProductVersionVariablesResolverDescriptorOutboundPort descriptorPort) {
        this.command = command;
        this.presenter = presenter;
        this.persistencePort = persistencePort;
        this.descriptorPort = descriptorPort;
    }

    @Override
    public void execute() {
        validateCommand(command);
        DataProductVersion dataProductVersion = persistencePort.findByUuid(command.dataProductVersionUuid());

        if (dataProductVersion == null) {
            throw new BadRequestException("DataProductVersion not found: " + command.dataProductVersionUuid());
        }

        List<DescriptorVariable> descriptorVariables = persistencePort.findDescriptorVariables(command.dataProductVersionUuid());
        JsonNode resolvedContent = descriptorPort.resolveDescriptor(dataProductVersion, descriptorVariables);

        presenter.presentDataProductVersionResolvedContent(dataProductVersion, resolvedContent);
    }

    private void validateCommand(DataProductVersionVariablesResolverCommand command) {
        if (command == null) {
            throw new BadRequestException("DataProductVersionVariablesResolverCommand cannot be null");
        }

        if (!StringUtils.hasText(command.dataProductVersionUuid())) {
            throw new BadRequestException("DataProductVersion UUID is required for resolving variables");
        }
    }
}
