package org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

@Mapper(componentModel = "spring")
public interface DescriptorVariableMapper {
    
    DescriptorVariable toEntity(DescriptorVariableRes res);
    
    DescriptorVariableRes toRes(DescriptorVariable entity);
}
