package org.opendatamesh.platform.pp.registry.githandler.provider.azure.credentials;

import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;

public class AzurePatCredential implements GitProviderCredential {

    private final String token;

    public AzurePatCredential(String token) {
        this.token = token;
    }

    @Override
    public GitAuthContext createGitAuthContext() {
        GitAuthContext ctx = new GitAuthContext();
        ctx.transportProtocol = GitAuthContext.TransportProtocol.HTTP;
        HttpHeaders headers = new HttpHeaders();
        // For Azure DevOps, we need to use basic auth with PAT as password
        // Azure DevOps uses username:token format for basic auth
        headers.set("username", "dummy"); // Azure DevOps doesn't use username for PAT auth
        headers.set("password", token);
        ctx.httpAuthHeaders = headers;

        return ctx;
    }

    @Override
    public HttpHeaders createGitProviderHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("dummy", token);

        // Add common headers for Azure DevOps API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");

        return headers;
    }
}

