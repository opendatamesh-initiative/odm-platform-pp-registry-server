package org.opendatamesh.platform.pp.registry.githandler.git;

import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;

import java.io.File;

public interface GitOperation {
    File getRepositoryContent(RepositoryPointer pointer, GitAuthContext ctx);
}
