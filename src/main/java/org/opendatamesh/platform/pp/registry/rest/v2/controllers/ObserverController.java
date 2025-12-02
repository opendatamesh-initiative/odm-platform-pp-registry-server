package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.observer.ObserverService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.notification.NotificationDispatchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/up/observer", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Products", description = "Endpoints for events")
public class ObserverController {

    @Autowired
    private ObserverService observerService;

    @Operation(summary = "Receive a notification event", description = "Receives a notification event from an observer server and dispatches it to the appropriate use case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification event received and dispatched successfully",
                    content = @Content(schema = @Schema(implementation = NotificationDispatchRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public void receiveEvent(
            @Parameter(description = "Notification event", required = true)
            @RequestBody NotificationDispatchRes notification
    ) {
        observerService.dispatchNotification(notification);
    }
}
