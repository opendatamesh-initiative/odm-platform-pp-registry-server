package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public record DataProductVersionRejectCommand(DataProductVersion dataProductVersion) {
}
