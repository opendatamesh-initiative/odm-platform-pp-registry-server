package org.opendatamesh.platform.pp.registry.client.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventEmitCommandRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.SubscribeRequestRes;
import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationClientImplTest {

    @Mock
    private RestUtils restUtils;

    private NotificationClientImpl notificationClient;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String OBSERVER_NAME = "test-observer";
    private static final String OBSERVER_DISPLAY_NAME = "Test Observer";
    private static final String NOTIFICATION_SERVICE_BASE_URL = "http://notification-service:8080";

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClientImpl(
                BASE_URL,
                OBSERVER_NAME,
                OBSERVER_DISPLAY_NAME,
                NOTIFICATION_SERVICE_BASE_URL,
                restUtils
        );
    }

    @Test
    void whenAssertConnectionSucceedsThenNoExceptionThrown() throws ClientException {
        // Given
        when(restUtils.genericGet(anyString(), isNull(), isNull(), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.assertConnection();

        // Then
        verify(restUtils).genericGet(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/actuator/health"),
                isNull(),
                isNull(),
                eq(Object.class)
        );
    }

    @Test
    void whenAssertConnectionFailsThenThrowIllegalStateException() throws ClientException {
        // Given
        ClientException clientException = new ClientException(500, "Service unavailable");
        when(restUtils.genericGet(anyString(), isNull(), isNull(), eq(Object.class)))
                .thenThrow(clientException);

        // When & Then
        assertThatThrownBy(() -> notificationClient.assertConnection())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to check connection to notification service")
                .hasCause(clientException);

        verify(restUtils).genericGet(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/actuator/health"),
                isNull(),
                isNull(),
                eq(Object.class)
        );
    }

    @Test
    void whenSubscribeToEventsSucceedsThenSubscriptionRequestIsSent() throws ClientException {
        // Given
        when(restUtils.genericPost(anyString(), isNull(), any(SubscribeRequestRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.subscribeToEvents();

        // Then
        ArgumentCaptor<SubscribeRequestRes> requestCaptor = ArgumentCaptor.forClass(SubscribeRequestRes.class);
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/subscriptions/subscribe"),
                isNull(),
                requestCaptor.capture(),
                eq(Object.class)
        );

        SubscribeRequestRes capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.getObserverBaseUrl()).isEqualTo(BASE_URL);
        assertThat(capturedRequest.getObserverName()).isEqualTo(OBSERVER_NAME);
        assertThat(capturedRequest.getObserverDisplayName()).isEqualTo(OBSERVER_DISPLAY_NAME);
        assertThat(capturedRequest.getObserverApiVersion()).isEqualTo("V2");
        assertThat(capturedRequest.getEventTypes()).isNotNull();
        assertThat(capturedRequest.getEventTypes()).containsExactlyInAnyOrder(
                "DATA_PRODUCT_INITIALIZATION_APPROVED",
                "DATA_PRODUCT_INITIALIZATION_REJECTED",
                "DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED",
                "DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED"
        );
    }

    @Test
    void whenSubscribeToEventsFailsThenThrowIllegalStateException() throws ClientException {
        // Given
        ClientException clientException = new ClientException(500, "Subscription failed");
        when(restUtils.genericPost(anyString(), isNull(), any(SubscribeRequestRes.class), eq(Object.class)))
                .thenThrow(clientException);

        // When & Then
        assertThatThrownBy(() -> notificationClient.subscribeToEvents())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to subscribe to events")
                .hasCause(clientException);

        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/subscriptions/subscribe"),
                isNull(),
                any(SubscribeRequestRes.class),
                eq(Object.class)
        );
    }

    @Test
    void whenNotifyEventSucceedsThenEventIsEmitted() throws ClientException {
        // Given
        EventRes event = createTestEvent();
        when(restUtils.genericPost(anyString(), isNull(), any(EventEmitCommandRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.notifyEvent(event);

        // Then
        ArgumentCaptor<EventEmitCommandRes> commandCaptor = ArgumentCaptor.forClass(EventEmitCommandRes.class);
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/events/emit"),
                isNull(),
                commandCaptor.capture(),
                eq(Object.class)
        );

        EventEmitCommandRes capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand).isNotNull();
        if (capturedCommand.getEvent() != null) {
            assertThat(capturedCommand.getEvent()).isEqualTo(event);
        }
    }

    @Test
    void whenNotifyEventFailsThenExceptionIsLoggedButNotThrown() throws ClientException {
        // Given
        EventRes event = createTestEvent();
        ClientException clientException = new ClientException(500, "Event emission failed");
        when(restUtils.genericPost(anyString(), isNull(), any(EventEmitCommandRes.class), eq(Object.class)))
                .thenThrow(clientException);

        // When
        notificationClient.notifyEvent(event);

        // Then
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/events/emit"),
                isNull(),
                any(EventEmitCommandRes.class),
                eq(Object.class)
        );
        // Verify that no exception is thrown (unlike assertConnection and subscribeToEvents)
    }

    @Test
    void whenNotifyEventWithNullEventThenEventEmitCommandIsStillCreated() throws ClientException {
        // Given
        EventRes event = null;
        when(restUtils.genericPost(anyString(), isNull(), any(EventEmitCommandRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.notifyEvent(event);

        // Then
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/events/emit"),
                isNull(),
                any(EventEmitCommandRes.class),
                eq(Object.class)
        );
    }

    @Test
    void whenSubscribeToEventsThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        when(restUtils.genericPost(anyString(), isNull(), any(SubscribeRequestRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.subscribeToEvents();

        // Then
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/subscriptions/subscribe"),
                isNull(),
                any(SubscribeRequestRes.class),
                eq(Object.class)
        );
    }

    @Test
    void whenNotifyEventThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        EventRes event = createTestEvent();
        when(restUtils.genericPost(anyString(), isNull(), any(EventEmitCommandRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.notifyEvent(event);

        // Then
        verify(restUtils).genericPost(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/events/emit"),
                isNull(),
                any(EventEmitCommandRes.class),
                eq(Object.class)
        );
    }

    @Test
    void whenAssertConnectionThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        when(restUtils.genericGet(anyString(), isNull(), isNull(), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.assertConnection();

        // Then
        verify(restUtils).genericGet(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/actuator/health"),
                isNull(),
                isNull(),
                eq(Object.class)
        );
    }

    private EventRes createTestEvent() {
        EventRes event = new EventRes();
        event.setResourceType(EventRes.ResourceType.DATA_PRODUCT);
        event.setResourceIdentifier("test.domain:test-product");
        return event;
    }
}

