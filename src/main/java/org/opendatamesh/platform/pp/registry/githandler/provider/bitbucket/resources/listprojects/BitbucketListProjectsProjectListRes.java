package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects;

import java.util.List;

public class BitbucketListProjectsProjectListRes {
    private List<BitbucketListProjectsProjectRes> values;
    private Integer page;
    private Integer pagelen;
    private Integer size;

    public BitbucketListProjectsProjectListRes() {
    }

    public BitbucketListProjectsProjectListRes(List<BitbucketListProjectsProjectRes> values, Integer page, Integer pagelen, Integer size) {
        this.values = values;
        this.page = page;
        this.pagelen = pagelen;
        this.size = size;
    }

    public List<BitbucketListProjectsProjectRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListProjectsProjectRes> values) {
        this.values = values;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPagelen() {
        return pagelen;
    }

    public void setPagelen(Integer pagelen) {
        this.pagelen = pagelen;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}

