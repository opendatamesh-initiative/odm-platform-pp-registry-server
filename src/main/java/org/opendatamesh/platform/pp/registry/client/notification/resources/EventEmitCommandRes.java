package org.opendatamesh.platform.pp.registry.client.notification.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public class EventEmitCommandRes {
    @Schema(description = "Event details to be emitted", required = true)
    EventRes event;

    public EventEmitCommandRes(EventRes event) {
        this.event = event;
    }

    public EventRes getEvent() {
        return event;
    }

    public void setEvent(EventRes event) {
        this.event = event;
    }

}
