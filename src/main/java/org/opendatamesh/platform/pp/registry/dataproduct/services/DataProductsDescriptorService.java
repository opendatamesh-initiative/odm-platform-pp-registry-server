package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.GetDescriptorOptionsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.InitDescriptorCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.UpdateDescriptorCommandRes;
import org.springframework.http.HttpHeaders;

/**
 * This service is used to interact with the descriptor file located in the
 * git repository configured in a Data Product.
 */
public interface DataProductsDescriptorService {

    JsonNode getDescriptor(String dataProductUuid, GetDescriptorOptionsRes options, HttpHeaders headers);

    void initDescriptor(String dataProductUuid, JsonNode content, InitDescriptorCommandRes options, HttpHeaders headers);

    void updateDescriptor(String dataProductUuid, JsonNode content, UpdateDescriptorCommandRes options, HttpHeaders headers);
}
