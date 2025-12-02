package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AzureListCommitsBatchReqRes {
    
    @JsonProperty("itemVersion")
    private VersionReference itemVersion;
    
    @JsonProperty("compareVersion")
    private VersionReference compareVersion;
    
    public AzureListCommitsBatchReqRes() {}
    
    public AzureListCommitsBatchReqRes(VersionReference itemVersion, VersionReference compareVersion) {
        this.itemVersion = itemVersion;
        this.compareVersion = compareVersion;
    }
    
    public VersionReference getItemVersion() {
        return itemVersion;
    }
    
    public void setItemVersion(VersionReference itemVersion) {
        this.itemVersion = itemVersion;
    }
    
    public VersionReference getCompareVersion() {
        return compareVersion;
    }
    
    public void setCompareVersion(VersionReference compareVersion) {
        this.compareVersion = compareVersion;
    }
    
    public static class VersionReference {
        @JsonProperty("versionType")
        private String versionType;
        
        @JsonProperty("version")
        private String version;
        
        public VersionReference() {}
        
        public VersionReference(String versionType, String version) {
            this.versionType = versionType;
            this.version = version;
        }
        
        public String getVersionType() {
            return versionType;
        }
        
        public void setVersionType(String versionType) {
            this.versionType = versionType;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
    }
}

