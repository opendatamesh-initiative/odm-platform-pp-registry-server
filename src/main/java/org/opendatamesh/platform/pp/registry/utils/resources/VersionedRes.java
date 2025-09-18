package org.opendatamesh.platform.pp.registry.utils.resources;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class VersionedRes {

    @Schema(description = "The creation timestamp. Automatically handled by the API: can not be modified.")
    private Date createdAt;

    @Schema(description = "The last update timestamp. Automatically handled by the API: can not be modified.")
    private Date updatedAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
