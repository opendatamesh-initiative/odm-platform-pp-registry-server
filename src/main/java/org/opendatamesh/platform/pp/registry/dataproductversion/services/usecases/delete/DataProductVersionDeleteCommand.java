package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

public record DataProductVersionDeleteCommand(String dataProductVersionUuid, String dataProductFqn, String dataProductVersionTag) {
}

