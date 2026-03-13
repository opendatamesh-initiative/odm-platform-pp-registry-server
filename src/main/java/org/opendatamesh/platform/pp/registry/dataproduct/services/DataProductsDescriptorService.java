package org.opendatamesh.platform.pp.registry.dataproduct.services;

import java.util.Optional;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;


public interface DataProductsDescriptorService {
    Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference pointer, HttpHeaders headers);

    void initDescriptor(String dataProductUuid, JsonNode content, HttpHeaders headers, String branch, String authorName, String authorEmail);

    void updateDescriptor(String dataProductUuid, String branch, String commitMessage, String baseCommit, JsonNode content, HttpHeaders headers, String authorName, String authorEmail);

}
