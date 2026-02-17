package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.TagRes;
import org.springframework.http.HttpHeaders;

import java.util.Optional;


public interface DataProductsDescriptorService {
    Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference pointer, HttpHeaders headers);

    void initDescriptor(String dataProductUuid, JsonNode content, HttpHeaders headers, String branch);

    void updateDescriptor(String dataProductUuid, String branch, String commitMessage, String baseCommit, JsonNode content, HttpHeaders headers);

    TagRes addTag(String dataProductUuid, TagRes tagRes, HttpHeaders headers);

}
