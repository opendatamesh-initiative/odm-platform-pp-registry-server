package org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider;

/**
 * Authentication context for bearer token authentication
 */
public class PatCredential {
    //Optional field
    private String username;
    private String token;

    public PatCredential() {
    }

    public PatCredential(String token) {
        this.token = token;
    }

    public PatCredential(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
