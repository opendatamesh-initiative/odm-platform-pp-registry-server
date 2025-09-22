package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproduct.resources.DataProductRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.utils.services.GenericCrudService;


public interface DataProductService extends GenericCrudService<DataProductRes, String> {
    public JsonNode getDescriptor(String uuid, VersionPointer pointer, PatCredential credential);
}
