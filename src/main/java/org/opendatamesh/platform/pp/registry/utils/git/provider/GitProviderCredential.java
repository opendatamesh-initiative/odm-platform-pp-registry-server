package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.opendatamesh.platform.pp.registry.utils.git.git.GitCredential;
import org.springframework.http.HttpHeaders;

public interface GitProviderCredential {

    GitCredential createGitCredential();

    HttpHeaders createGitProviderHeaders();
}
