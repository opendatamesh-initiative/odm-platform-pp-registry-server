package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventEmitCommandRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.SubscribeRequestRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class NotificationClientImpl implements NotificationClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Notification service endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String SUBSCRIBE_ENDPOINT = "/api/v2/pp/notification/subscriptions/subscribe";
    private static final String NOTIFICATION_ENDPOINT = "/api/v2/pp/notification/events/emit";

    // Observer configuration
    private final String baseUrl;
    private final String observerName;
    private final String observerDisplayName;
    private static final String OBSERVER_API_VERSION = "V2"; // Always v2
    private static final List<String> EVENT_TYPES = Arrays.asList("DATA_PRODUCT_INITIALIZATION_APPROVED", "DATA_PRODUCT_INITIALIZATION_REJECTED", "DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED", "DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED");
    private static final List<String> POLICY_EVENT_TYPES = Arrays.asList("DATA_PRODUCT_INITIALIZATION_REQUESTED", "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED");

    private final RestUtils restUtils;
    private final String notificationServiceBaseUrl;

    NotificationClientImpl(String baseUrl, String observerName, String observerDisplayName, String notificationServiceBaseUrl, RestUtils restUtils) {
        this.baseUrl = baseUrl;
        this.observerName = observerName;
        this.observerDisplayName = observerDisplayName;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
        this.restUtils = restUtils;
    }

    @Override
    public void assertConnection() {
        try {
            restUtils.genericGet(String.format("%s%s", notificationServiceBaseUrl, HEALTH_ENDPOINT), null, null, Object.class);
        } catch (ClientException e) {
            log.warn("Failed to check connection to notification service: {}", e.getMessage());
            // ClientException
            throw new IllegalStateException("Failed to check connection to notification service", e);
        }
    }

    @Override
    public void subscribeToEvents() {
        try {
            SubscribeRequestRes req = createSubscribeRequest();
            restUtils.genericPost(String.format("%s%s", notificationServiceBaseUrl, SUBSCRIBE_ENDPOINT), null, req, Object.class);
            log.info("Subscribed to events: {}", req.getEventTypes());
        } catch (ClientException e) {
            log.warn("Failed to subscribe to events: {}", e.getMessage());
            throw new IllegalStateException("Failed to subscribe to events", e);
        }
    }

    @Override
    public void notifyEvent(EventRes event) {
        try {
            EventEmitCommandRes req = new EventEmitCommandRes(event);
            restUtils.genericPost(
                    String.format("%s%s", notificationServiceBaseUrl, NOTIFICATION_ENDPOINT),
                    null,
                    req,
                    Object.class
            );
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private SubscribeRequestRes createSubscribeRequest() {
        SubscribeRequestRes req = new SubscribeRequestRes();
        req.setObserverBaseUrl(baseUrl);
        req.setObserverName(observerName);
        req.setObserverDisplayName(observerDisplayName);
        req.setObserverApiVersion(OBSERVER_API_VERSION);
        req.setEventTypes(EVENT_TYPES);
        // TODO: add POLICY_EVENT_TYPES if policy server is not available
        return req;
    }
}