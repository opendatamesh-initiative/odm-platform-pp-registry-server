package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.TagRequestRes;

import java.util.Optional;


public interface DataProductsDescriptorService {
    Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference pointer, Credential credential);

    void initDescriptor(String dataProductUuid, JsonNode content, Credential credential);

    void updateDescriptor(String dataProductUuid, String branch, String commitMessage, String baseCommit, JsonNode content, Credential credential);

    /**
     * Create a tag for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param credential PAT credentials for authentication
     * @param tagRes for the tag detail
     */
    void createTag(String dataProductUuid, Credential credential, TagRequestRes tagRes);

}
