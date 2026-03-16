package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.credentials;

import org.opendatamesh.platform.pp.registry.utils.git.git.GitCredential;
import org.opendatamesh.platform.pp.registry.utils.git.git.GitCredentialHttps;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;

public class BitbucketPatCredential implements GitProviderCredential {

    private final String username;
    private final String token;

    public BitbucketPatCredential(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    @Override
    public GitCredential createGitCredential() {
        GitCredentialHttps ctx = new GitCredentialHttps();
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", username);
        headers.set("password", token);
        ctx.setHttpAuthHeaders(headers);
        return ctx;
    }

    @Override
    public HttpHeaders createGitProviderHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, token);

        // Add common headers for Bitbucket API
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");
        headers.set("X-Atlassian-Username", username);

        return headers;
    }
}

