package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResourceDefinition;

@Mapper(componentModel = "spring")
public interface ProviderCustomResourceDefinitionMapper {

    ProviderCustomResourceDefinitionRes toRes(ProviderCustomResourceDefinition definition);
}

