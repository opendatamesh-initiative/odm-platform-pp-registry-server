package org.opendatamesh.platform.pp.registry.client.notification;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventEmitCommandRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.NotificationRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.SubscribeRequestRes;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NotificationClientImpl implements NotificationClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Notification service endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String SUBSCRIBE_ENDPOINT = "/api/v2/pp/notification/subscriptions/subscribe";
    private static final String EMIT_EVENT_ENDPOINT = "/api/v2/pp/notification/events/emit";
    private static final String NOTIFICATION_ENDPOINT = "/api/v2/pp/notification/notifications";

    // Observer configuration
    private final String baseUrl;
    private final String observerName;
    private final String observerDisplayName;
    private static final String OBSERVER_API_VERSION = "V2"; // Always v2

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
        restUtils.genericGet(String.format("%s%s", notificationServiceBaseUrl, HEALTH_ENDPOINT), null, null, Object.class);
    }

    @Override
    public void subscribeToEvents(List<String> eventTypes, List<String> policyEventTypes) {
        SubscribeRequestRes req = createSubscribeRequest(eventTypes, policyEventTypes);
        restUtils.genericPost(String.format("%s%s", notificationServiceBaseUrl, SUBSCRIBE_ENDPOINT), null, req, Object.class);
        log.info("Subscribed to events: {}", req.getEventTypes());
    }

    @Override
    public void notifyEvent(Object event) {
        EventEmitCommandRes req = new EventEmitCommandRes(event);
        restUtils.genericPost(
                String.format("%s%s", notificationServiceBaseUrl, EMIT_EVENT_ENDPOINT),
                null,
                req,
                Object.class
        );
    }

    @Override
    public void processingSuccess(Long notificationId) {
        NotificationRes notification = getNotification(notificationId);
        notification.setStatus(NotificationRes.NotificationStatusRes.PROCESSED);
        restUtils.put(String.format("%s%s/{id}", notificationServiceBaseUrl, NOTIFICATION_ENDPOINT), null, notificationId, notification, NotificationRes.class);
    }

    @Override
    public void processingFailure(Long notificationId) {
        NotificationRes notification = getNotification(notificationId);
        notification.setStatus(NotificationRes.NotificationStatusRes.FAILED_TO_PROCESS);
        restUtils.put(String.format("%s%s/{id}", notificationServiceBaseUrl, NOTIFICATION_ENDPOINT), null, notificationId, notification, NotificationRes.class);
    }

    private NotificationRes getNotification(Long notificationId) {
        return restUtils.get(String.format("%s%s/{id}", notificationServiceBaseUrl, NOTIFICATION_ENDPOINT), null, notificationId, NotificationRes.class);
    }

    private SubscribeRequestRes createSubscribeRequest(List<String> eventTypes, List<String> policyEventTypes) {
        SubscribeRequestRes req = new SubscribeRequestRes();
        req.setObserverBaseUrl(baseUrl);
        req.setName(observerName);
        req.setDisplayName(observerDisplayName);
        req.setObserverApiVersion(OBSERVER_API_VERSION);
        req.setEventTypes(eventTypes);
        return req;
    }
}