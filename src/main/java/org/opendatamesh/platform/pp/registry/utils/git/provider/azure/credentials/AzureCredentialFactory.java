package org.opendatamesh.platform.pp.registry.utils.git.provider.azure.credentials;

import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitProviderConfigurationException;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

public abstract class AzureCredentialFactory {
    private static final String CREDENTIAL_TYPE = "x-odm-gpauth-type";

    private AzureCredentialFactory() {
        //DO NOTHING
    }

    public static GitProviderCredential createCredentials(HttpHeaders headers) {
        String type = headers.getFirst(CREDENTIAL_TYPE);
        if (type == null) {
            throw new GitProviderConfigurationException("Missing header with name: " + CREDENTIAL_TYPE);
        }
        if (type.equalsIgnoreCase("PAT")) {
            return buildPat(headers);
        }
        throw new GitProviderConfigurationException("Unsupported Authentication Type for Azure DevOps: " + type);
    }

    private static AzurePatCredential buildPat(HttpHeaders headers) {
        String token = headers.getFirst("x-odm-gpauth-param-token");
        if (!StringUtils.hasText(token)) {
            throw new GitProviderConfigurationException("Header x-odm-gpauth-param-token could not be null");
        }
        return new AzurePatCredential(token);
    }
}

