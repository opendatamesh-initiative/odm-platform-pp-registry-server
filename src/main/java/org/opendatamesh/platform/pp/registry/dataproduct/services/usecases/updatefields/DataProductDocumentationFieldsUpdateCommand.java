package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;

public record DataProductDocumentationFieldsUpdateCommand(
        String uuid,
        String displayName,
        String description,
        DataProductRepo dataProductRepo
) {
    public String getUuid() {
        return uuid;
    }
}
