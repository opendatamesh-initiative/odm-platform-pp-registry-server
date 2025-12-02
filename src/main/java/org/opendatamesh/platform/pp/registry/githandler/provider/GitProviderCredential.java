package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.springframework.http.HttpHeaders;

public interface GitProviderCredential {

    GitAuthContext createGitAuthContext();

    HttpHeaders createGitProviderHeaders();
}
