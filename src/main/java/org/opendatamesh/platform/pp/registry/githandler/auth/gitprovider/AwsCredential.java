package org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider;

/**
 * Authentication context for AWS-based authentication
 */
public class AwsCredential implements Credential {

    private String awsAccessKeyId;
    private String awsSecretKey;
    private String awsSessionToken;
    private String region;

    public AwsCredential(String awsAccessKeyId, String awsSecretKey,
                         String awsSessionToken, String region) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretKey = awsSecretKey;
        this.awsSessionToken = awsSessionToken;
        this.region = region;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public String getAwsSessionToken() {
        return awsSessionToken;
    }

    public void setAwsSessionToken(String awsSessionToken) {
        this.awsSessionToken = awsSessionToken;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
