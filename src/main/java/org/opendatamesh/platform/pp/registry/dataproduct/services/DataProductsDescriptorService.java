package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.GetDescriptorOptionsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.InitDescriptorCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.UpdateDescriptorCommandRes;
import org.springframework.http.HttpHeaders;

public interface DataProductsDescriptorService {

    JsonNode getDescriptor(String uuid, GetDescriptorOptionsRes options, HttpHeaders headers);

    void initDescriptor(String uuid, JsonNode content, InitDescriptorCommandRes options, HttpHeaders headers);

    void updateDescriptor(String uuid, JsonNode content, UpdateDescriptorCommandRes options, HttpHeaders headers);
}
