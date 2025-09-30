package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.githandler.model.Tag;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagRes toRes(Tag tag);
}
