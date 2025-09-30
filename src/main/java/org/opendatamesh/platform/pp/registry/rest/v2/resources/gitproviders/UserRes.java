package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "user", description = "User resource representing a Git provider user")
public class UserRes {

    @Schema(description = "The unique identifier of the user", example = "12345678")
    private String id;

    @Schema(description = "The username of the user", example = "john.doe")
    private String username;

    public UserRes() {
    }

    public UserRes(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
