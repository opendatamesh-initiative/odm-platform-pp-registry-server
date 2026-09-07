package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionAdditionalTag;

@Mapper(componentModel = "spring")
public interface DataProductVersionAdditionalTagMapper {

    @Mapping(target = "sequenceId", ignore = true)
    @Mapping(target = "dataProductVersion", ignore = true)
    @Mapping(target = "dataProductVersionUuid", ignore = true)
    DataProductVersionAdditionalTag toEntity(DataProductVersionAdditionalTagRes res);

    DataProductVersionAdditionalTagRes toRes(DataProductVersionAdditionalTag entity);
}
