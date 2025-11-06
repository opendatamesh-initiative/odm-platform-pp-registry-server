package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResource;

@Mapper(componentModel = "spring")
public interface ProviderCustomResourceMapper {

    ProviderCustomResourceRes toRes(ProviderCustomResource providerCustomResource);
}

