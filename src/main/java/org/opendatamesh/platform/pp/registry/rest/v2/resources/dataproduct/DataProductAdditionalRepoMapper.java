package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoOwnerType;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;

@Mapper(componentModel = "spring")
public interface DataProductAdditionalRepoMapper {

    @Mapping(target = "dataProduct", ignore = true)
    DataProductAdditionalRepo toEntity(DataProductAdditionalRepoRes res);

    DataProductAdditionalRepoRes toRes(DataProductAdditionalRepo entity);

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

    default DataProductRepoProviderTypeRes map(DataProductRepoProviderType entity) {
        if (entity == null) {
            return null;
        }
        return DataProductRepoProviderTypeRes.valueOf(entity.name());
    }

    default DataProductRepoOwnerTypeRes map(DataProductRepoOwnerType entity) {
        if (entity == null) {
            return null;
        }
        return DataProductRepoOwnerTypeRes.valueOf(entity.name());
    }
}
