package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;

@Mapper(componentModel = "spring")
public interface DataProductVersionMapper {
    
    // Methods for full entity
    DataProductVersion toEntity(DataProductVersionRes res);
    
    DataProductVersionRes toRes(DataProductVersion entity);
    
    // Methods for short entity
    DataProductVersionShortRes toShortResFromShort(DataProductVersionShort entity);
}
