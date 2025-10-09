package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;

import java.util.Optional;


public interface DataProductsDescriptorService {
    Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference pointer, Credential credential);

    void initDescriptor(String dataProductUuid, JsonNode content, Credential credential);

    void updateDescriptor(String dataProductUuid, String branch, String commitMessage, String baseCommit, JsonNode content, Credential credential);
}
