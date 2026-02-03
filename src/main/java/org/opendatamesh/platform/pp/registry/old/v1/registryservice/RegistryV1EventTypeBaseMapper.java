package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface RegistryV1EventTypeBaseMapper {

    @Mapping(target = "dataProductVersion", source = "dataProductVersionDPDS")
    RegistryV1DataProductEventTypeResource toEventResource(DataProductVersionDPDS dataProductVersionDPDS);
}
