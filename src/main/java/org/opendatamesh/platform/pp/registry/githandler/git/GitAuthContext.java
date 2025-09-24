package org.opendatamesh.platform.pp.registry.githandler.git;

import org.springframework.http.HttpHeaders;

public class GitAuthContext {

    public enum TransportProtocol {
        SSH, HTTP
    }

    public TransportProtocol transportProtocol;
    public HttpHeaders httpAuthHeaders;
    public String sshPrivateKey;
    public String sshPublickey;
    public String sshuser;

    public GitAuthContext() {
    }

    public GitAuthContext(TransportProtocol transportProtocol, HttpHeaders httpAuthHeaders,
                          String sshPrivateKey, String sshPublickey, String sshuser) {
        this.transportProtocol = transportProtocol;
        this.httpAuthHeaders = httpAuthHeaders;
        this.sshPrivateKey = sshPrivateKey;
        this.sshPublickey = sshPublickey;
        this.sshuser = sshuser;
    }

    // Getters and setters
    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(TransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public HttpHeaders getHttpAuthHeaders() {
        return httpAuthHeaders;
    }

    public void setHttpAuthHeaders(HttpHeaders httpAuthHeaders) {
        this.httpAuthHeaders = httpAuthHeaders;
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public String getSshPublickey() {
        return sshPublickey;
    }

    public void setSshPublickey(String sshPublickey) {
        this.sshPublickey = sshPublickey;
    }

    public String getSshuser() {
        return sshuser;
    }

    public void setSshuser(String sshuser) {
        this.sshuser = sshuser;
    }
}
