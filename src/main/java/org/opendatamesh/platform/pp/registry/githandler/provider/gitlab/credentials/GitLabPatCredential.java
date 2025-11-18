package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.credentials;

import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;

public class GitLabPatCredential implements GitProviderCredential {

    private final String token;

    public GitLabPatCredential(String token) {
        this.token = token;
    }

    @Override
    public GitAuthContext createGitAuthContext() {
        GitAuthContext ctx = new GitAuthContext();
        ctx.transportProtocol = GitAuthContext.TransportProtocol.HTTP;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        ctx.httpAuthHeaders = headers;

        return ctx;
    }

    @Override
    public HttpHeaders createGitProviderHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Add common headers for GitLab API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");
        return headers;
    }
}

