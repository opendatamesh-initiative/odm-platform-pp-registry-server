package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendatamesh.platform.pp.registry.githandler.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserRes toRes(User user);

    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "url", ignore = true)
    User toEntity(UserRes userRes);
}
