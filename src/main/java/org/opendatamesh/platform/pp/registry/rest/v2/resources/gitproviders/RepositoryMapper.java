package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {

    RepositoryRes toRes(Repository repository);

    Repository toEntity(RepositoryRes repositoryRes);
}
