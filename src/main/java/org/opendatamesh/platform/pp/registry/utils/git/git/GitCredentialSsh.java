package org.opendatamesh.platform.pp.registry.utils.git.git;

public class GitCredentialSsh extends GitCredential {
    private String sshPrivateKey;
    private String sshPublickey;
    private String sshUser;

    public GitCredentialSsh() {
        super(TransportProtocol.SSH);
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

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }
}
