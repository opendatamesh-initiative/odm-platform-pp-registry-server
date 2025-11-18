package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.credentials;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

public abstract class BitbucketCredentialFactory {
    private static final String CREDENTIAL_TYPE = "x-odm-gpauth-type";

    private BitbucketCredentialFactory() {
        //DO NOTHING
    }

    public static GitProviderCredential createCredentials(HttpHeaders headers) throws BadRequestException {
        String type = headers.getFirst(CREDENTIAL_TYPE);
        if (type == null) {
            throw new BadRequestException("Missing header with name: " + CREDENTIAL_TYPE);
        }
        if (type.equalsIgnoreCase("PAT")) {
            return buildPat(headers);
        }
        throw new BadRequestException("Unsupported Authentication Type for Bitbucket: " + type);
    }

    private static BitbucketPatCredential buildPat(HttpHeaders headers) {
        String token = headers.getFirst("x-odm-gpauth-param-token");
        String username = headers.getFirst("x-odm-gpauth-param-username");
        if (!StringUtils.hasText(token)) {
            throw new BadRequestException("Header x-odm-gpauth-param-token could not be null");
        }
        if (!StringUtils.hasText(username)) {
            throw new BadRequestException("Header x-odm-gpauth-param-username could not be null");
        }
        return new BitbucketPatCredential(username, token);
    }
}

