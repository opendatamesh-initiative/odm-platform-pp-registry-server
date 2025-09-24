package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    OrganizationRes toResource(Organization organization);

    @Mapping(target = "members", ignore = true)
    @Mapping(target = "repositories", ignore = true)
    Organization toModel(OrganizationRes organizationRes);
}
