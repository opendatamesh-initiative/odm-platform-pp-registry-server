package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoOwnerType;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;

@Mapper(componentModel = "spring")
public interface DataProductRepoMapper {

    @Mapping(target = "dataProduct", ignore = true)
    DataProductRepo toEntity(DataProductRepoRes res);

    default DataProductRepoProviderType map(DataProductRepoProviderTypeRes res) {
        if (res == null) {
            return null;
        }
        return DataProductRepoProviderType.valueOf(res.name());
    }

    default DataProductRepoOwnerType map(DataProductRepoOwnerTypeRes res) {
        if (res == null) {
            return null;
        }
        return DataProductRepoOwnerType.valueOf(res.name());
    }
}
