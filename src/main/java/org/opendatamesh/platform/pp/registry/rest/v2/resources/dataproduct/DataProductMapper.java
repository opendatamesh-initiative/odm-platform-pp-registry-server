package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

@Mapper(componentModel = "spring")
public interface DataProductMapper {
    DataProduct toEntity(DataProductRes res);

    DataProductRes toRes(DataProduct entity);
}
