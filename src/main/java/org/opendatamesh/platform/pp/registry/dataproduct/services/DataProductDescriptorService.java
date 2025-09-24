package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;

import java.util.Optional;


public interface DataProductDescriptorService {
    Optional<JsonNode> getDescriptor(String uuid, VersionPointer pointer, Credential credential);
}
