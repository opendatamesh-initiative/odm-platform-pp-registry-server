package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.utils.usecases.NotificationEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/*
 *  This component is used to subscribe to event types that can be handled
 *  by the NotificationEventHandlers mounted in the Spring Application Context.
 *
 *  This component is design to fail the application startup if it fails tu subscribe to
 *  such events.
 *
 * */
@Component
public class NotificationClientEventSubscriber implements SmartInitializingSingleton {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<NotificationEventHandler> eventHandlers;
    @Autowired
    private NotificationClient notificationClient;

    @Override
    public void afterSingletonsInstantiated() {
        List<String> eventTypesToSubscribe = Arrays.stream(EventTypeRes.values())
                .filter(eventType -> eventHandlers.stream().anyMatch(a -> a.supportsEventType(eventType)))
                .map(Enum::name)
                .toList();
        logger.info("Subscribing registry to events: {}", eventTypesToSubscribe);
        notificationClient.subscribeToEvents(eventTypesToSubscribe);
    }
}
