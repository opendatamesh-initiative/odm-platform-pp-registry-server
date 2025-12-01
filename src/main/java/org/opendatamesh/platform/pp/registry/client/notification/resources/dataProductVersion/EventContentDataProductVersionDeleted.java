package org.opendatamesh.platform.pp.registry.client.notification.resources.dataProductVersion;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventContent;

public class EventContentDataProductVersionDeleted implements EventContent {
    private String dataProductVersionUuid;
    private String dataProductFqn;
    private String dataProductVersionTag;

    public EventContentDataProductVersionDeleted(String dataProductVersionUuid, String dataProductFqn, String dataProductVersionTag) {
        this.dataProductVersionUuid = dataProductVersionUuid;
        this.dataProductFqn = dataProductFqn;
        this.dataProductVersionTag = dataProductVersionTag;
    }
    
    public String getDataProductVersionUuid() {
        return dataProductVersionUuid;
    }

    public void setDataProductVersionUuid(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }
    
    public String getDataProductFqn() {
        return dataProductFqn;
    }

    public void setDataProductFqn(String dataProductFqn) {
        this.dataProductFqn = dataProductFqn;
    }

    public String getDataProductVersionTag() {
        return dataProductVersionTag;
    }

    public void setDataProductVersionTag(String dataProductVersionTag) {
        this.dataProductVersionTag = dataProductVersionTag;
    }
}
