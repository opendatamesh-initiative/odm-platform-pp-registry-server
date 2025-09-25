package org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider;

import java.util.Map;
import java.util.Optional;

public final class CredentialFactory {

    private CredentialFactory() {
    }

    public static Optional<Credential> fromHeaders(Map<String, String> headers) {

        String type = headers.get("x-odm-gpauth-type");
        if (type == null) {
            return Optional.empty();
        }

        switch (type.toUpperCase()) {
            case "PAT":
                String username = headers.get("x-odm-gpauth-param-username");
                String token = headers.get("x-odm-gpauth-param-token");
                return Optional.of((username != null) ? new PatCredential(username, token)
                        : new PatCredential(token));

            /*case "OAUTH":
                return new OauthCredential(
                        headers.get("x-odm-gpauth-param-url"),
                        headers.get("x-odm-gpauth-param-grant-type"),
                        headers.get("x-odm-gpauth-param-scope"),
                        headers.get("x-odm-gpauth-param-client-id"),
                        headers.get("x-odm-gpauth-param-client-secret"),
                        headers.get("x-odm-gpauth-param-client-certificate"),
                        headers.get("x-odm-gpauth-param-client-certificate-private-key")
                );*/

            default:
                return Optional.empty();
        }
    }
}

