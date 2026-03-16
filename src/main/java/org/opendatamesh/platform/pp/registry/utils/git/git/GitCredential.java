package org.opendatamesh.platform.pp.registry.utils.git.git;

public abstract class GitCredential {
    protected final TransportProtocol transportProtocol;

    public GitCredential(TransportProtocol transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    // Getters and setters
    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    public enum TransportProtocol {
        SSH, HTTP
    }

}
