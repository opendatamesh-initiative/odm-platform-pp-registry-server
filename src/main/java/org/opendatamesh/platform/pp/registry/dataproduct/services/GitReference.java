package org.opendatamesh.platform.pp.registry.dataproduct.services;

public class GitReference {

    public enum VersionType {
        TAG,
        BRANCH,
        COMMIT
    }

    private String tag;
    private String branch;
    private String commit;

    private VersionType type; // the resolved type
    private String value;     // the resolved value

    public GitReference() {
    }

    public GitReference(String tag, String branch, String commit) {
        this.tag = tag;
        this.branch = branch;
        this.commit = commit;
        resolve();
    }

    // getters and setters
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        resolve();
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
        resolve();
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
        resolve();
    }

    public VersionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    /**
     * Resolve the first non-null value, defaulting to branch if nothing is provided
     */
    private void resolve() {
        if (tag != null) {
            type = VersionType.TAG;
            value = tag;
        } else if (branch != null) {
            type = VersionType.BRANCH;
            value = branch;
        } else if (commit != null) {
            type = VersionType.COMMIT;
            value = commit;
        } else {
            type = VersionType.BRANCH;
            value = "main";
        }
    }
}


