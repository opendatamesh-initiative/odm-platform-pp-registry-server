package org.opendatamesh.platform.pp.registry.githandler.provider.github.credentials;

import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;

public class GitHubPatCredential implements GitProviderCredential {

    private final String token;

    public GitHubPatCredential(String token) {
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

        // Add common headers for GitHub API
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "GitProviderDemo/1.0");
        return headers;
    }
}
