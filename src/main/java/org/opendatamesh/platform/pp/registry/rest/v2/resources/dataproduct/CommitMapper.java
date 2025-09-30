package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.githandler.model.Commit;

@Mapper(componentModel = "spring")
public interface CommitMapper {
    CommitRes toRes(Commit commit);
}
