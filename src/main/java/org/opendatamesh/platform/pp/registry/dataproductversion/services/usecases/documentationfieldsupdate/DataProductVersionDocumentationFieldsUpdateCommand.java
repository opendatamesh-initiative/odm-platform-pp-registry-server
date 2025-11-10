package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsRes;

public record DataProductVersionDocumentationFieldsUpdateCommand(
        String uuid,
        String name,
        String description,
        String updatedBy
) {
    public String getUuid() {
        return uuid;
    }
}