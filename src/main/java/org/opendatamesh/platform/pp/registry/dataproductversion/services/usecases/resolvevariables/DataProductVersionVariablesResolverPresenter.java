package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public interface DataProductVersionVariablesResolverPresenter {

    void presentDataProductVersionResolvedContent(DataProductVersion productVersion, JsonNode resolvedContent);
}
