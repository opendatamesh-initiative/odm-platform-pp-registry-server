package org.opendatamesh.platform.pp.registry.client.notification.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public class EventEmitCommandRes {
    @Schema(description = "Event details to be emitted", required = true)
    Object event;

    public EventEmitCommandRes(Object event) {
        this.event = event;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = event;
    }

}
