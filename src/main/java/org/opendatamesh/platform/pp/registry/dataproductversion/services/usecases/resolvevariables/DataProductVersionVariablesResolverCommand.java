package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

public record DataProductVersionVariablesResolverCommand(String dataProductVersionUuid) {
    public String getUuid() {
        return dataProductVersionUuid;
    }
}
