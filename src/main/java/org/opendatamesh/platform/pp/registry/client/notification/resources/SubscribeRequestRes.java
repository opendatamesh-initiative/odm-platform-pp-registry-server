package org.opendatamesh.platform.pp.registry.client.notification.resources;

import java.util.List;

public class SubscribeRequestRes {
    private String observerName;
    private String observerDisplayName;
    private String observerBaseUrl;
    private String observerApiVersion;
    private List<String> eventTypes;

    public SubscribeRequestRes() {
    }

    public SubscribeRequestRes(String observerName, String observerDisplayName, String observerBaseUrl, String observerApiVersion, List<String> eventTypes) {
        this.observerName = observerName;
        this.observerDisplayName = observerDisplayName;
        this.observerBaseUrl = observerBaseUrl;
        this.observerApiVersion = observerApiVersion;
        this.eventTypes = eventTypes;
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }

    public String getObserverDisplayName() {
        return observerDisplayName;
    }

    public void setObserverDisplayName(String observerDisplayName) {
        this.observerDisplayName = observerDisplayName;
    }

    public String getObserverBaseUrl() {
        return observerBaseUrl;
    }

    public void setObserverBaseUrl(String observerBaseUrl) {
        this.observerBaseUrl = observerBaseUrl;
    }

    public String getObserverApiVersion() {
        return observerApiVersion;
    }

    public void setObserverApiVersion(String observerApiVersion) {
        this.observerApiVersion = observerApiVersion;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }
}
