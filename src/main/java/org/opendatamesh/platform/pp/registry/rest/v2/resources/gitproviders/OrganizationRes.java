package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "organization")
public class OrganizationRes {

    @Schema(description = "The unique identifier of the organization")
    private String id;

    @Schema(description = "The name of the organization")
    private String name;

    @Schema(description = "The URL of the organization")
    private String url;

    public OrganizationRes() {
    }

    public OrganizationRes(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "OrganizationRes{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
