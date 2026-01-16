package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve.ResolveDataProductVersionResultRes;

@Mapper(componentModel = "spring")
public interface DataProductVersionMapper {
    
    // Methods for full entity
    DataProductVersion toEntity(DataProductVersionRes res);
    
    DataProductVersionRes toRes(DataProductVersion entity);
    
    // Methods for short entity
    DataProductVersionShortRes toShortResFromShort(DataProductVersionShort entity);
    
    // Method for resolved data product version
    @Mapping(target = "resolvedContent", source = "resolvedContent")
    ResolveDataProductVersionResultRes.ResolvedDataProductVersionRes toResolvedRes(DataProductVersion entity, JsonNode resolvedContent);
}
