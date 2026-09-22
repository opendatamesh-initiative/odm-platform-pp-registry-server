package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;

import java.util.List;

public record DataProductDocumentationFieldsUpdateCommand(
        String uuid,
        String displayName,
        String description,
        DataProductRepo dataProductRepo,
        List<DataProductAdditionalRepo> additionalDataProductRepos
) {
    public String getUuid() {
        return uuid;
    }
}
