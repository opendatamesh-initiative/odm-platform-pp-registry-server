package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsRes;

public record DataProductVersionDocumentationFieldsUpdateCommand(
        DataProductVersionDocumentationFieldsRes documentationFieldsRes
) {
    public String getUuid() {
        return documentationFieldsRes != null ? documentationFieldsRes.getUuid() : null;
    }
}