package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.git.model.ProviderCustomResource;

@Mapper(componentModel = "spring")
public interface ProviderCustomResourceMapper {

    ProviderCustomResourceRes toRes(ProviderCustomResource providerCustomResource);
}

