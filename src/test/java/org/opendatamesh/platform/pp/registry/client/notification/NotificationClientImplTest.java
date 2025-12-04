package org.opendatamesh.platform.pp.registry.client.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.client.notification.resources.EventEmitCommandRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.NotificationRes;
import org.opendatamesh.platform.pp.registry.client.notification.resources.SubscribeRequestRes;
import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationClientImplTest {

    @Mock
    private RestUtils restUtils;

    private NotificationClientImpl notificationClient;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String OBSERVER_NAME = "test-observer";
    private static final String OBSERVER_DISPLAY_NAME = "Test Observer";
    private static final String NOTIFICATION_SERVICE_BASE_URL = "http://notification-service:8080";
    private static final List<String> EVENT_TYPES = Arrays.asList(
            "DATA_PRODUCT_INITIALIZATION_APPROVED",
            "DATA_PRODUCT_INITIALIZATION_REJECTED",
            "DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED",
            "DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED"
    );
    private static final List<String> POLICY_EVENT_TYPES = Arrays.asList(
            "DATA_PRODUCT_INITIALIZATION_REQUESTED",
            "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED"
    );

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
    void whenSubscribeToEventsSucceedsThenSubscriptionRequestIsSent() throws ClientException {
        // Given
        when(restUtils.genericPost(anyString(), isNull(), any(SubscribeRequestRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.subscribeToEvents(EVENT_TYPES, POLICY_EVENT_TYPES);

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
    void whenNotifyEventSucceedsThenEventIsEmitted() throws ClientException {
        // Given
        Object event = createTestEvent();
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
        assertThat(capturedCommand.getEvent()).isEqualTo(event);
    }

    @Test
    void whenSubscribeToEventsThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        when(restUtils.genericPost(anyString(), isNull(), any(SubscribeRequestRes.class), eq(Object.class)))
                .thenReturn(new Object());

        // When
        notificationClient.subscribeToEvents(EVENT_TYPES, POLICY_EVENT_TYPES);

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
        Object event = createTestEvent();
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

    @Test
    void whenProcessingSuccessThenCorrectEndpointsAreUsed() throws ClientException {
        // Given
        Long notificationId = 123L;
        NotificationRes notification = new NotificationRes();
        notification.setSequenceId(notificationId);
        notification.setStatus(NotificationRes.NotificationStatusRes.PROCESSING);

        when(restUtils.get(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                eq(NotificationRes.class)
        )).thenReturn(notification);

        when(restUtils.put(
                anyString(),
                isNull(),
                anyLong(),
                any(NotificationRes.class),
                eq(NotificationRes.class)
        )).thenReturn(notification);

        // When
        notificationClient.processingSuccess(notificationId);

        // Then - Verify GET is called to retrieve the notification
        verify(restUtils).get(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                eq(NotificationRes.class)
        );

        // Then - Verify PUT is called with PROCESSED status
        ArgumentCaptor<NotificationRes> notificationCaptor = ArgumentCaptor.forClass(NotificationRes.class);
        verify(restUtils).put(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                notificationCaptor.capture(),
                eq(NotificationRes.class)
        );

        NotificationRes capturedNotification = notificationCaptor.getValue();
        assertThat(capturedNotification).isNotNull();
        assertThat(capturedNotification.getStatus()).isEqualTo(NotificationRes.NotificationStatusRes.PROCESSED);
    }

    @Test
    void whenProcessingFailureThenCorrectEndpointsAreUsed() throws ClientException {
        // Given
        Long notificationId = 456L;
        NotificationRes notification = new NotificationRes();
        notification.setSequenceId(notificationId);
        notification.setStatus(NotificationRes.NotificationStatusRes.PROCESSING);

        when(restUtils.get(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                eq(NotificationRes.class)
        )).thenReturn(notification);

        when(restUtils.put(
                anyString(),
                isNull(),
                anyLong(),
                any(NotificationRes.class),
                eq(NotificationRes.class)
        )).thenReturn(notification);

        // When
        notificationClient.processingFailure(notificationId);

        // Then - Verify GET is called to retrieve the notification
        verify(restUtils).get(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                eq(NotificationRes.class)
        );

        // Then - Verify PUT is called with FAILED_TO_PROCESS status
        ArgumentCaptor<NotificationRes> notificationCaptor = ArgumentCaptor.forClass(NotificationRes.class);
        verify(restUtils).put(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                notificationCaptor.capture(),
                eq(NotificationRes.class)
        );

        NotificationRes capturedNotification = notificationCaptor.getValue();
        assertThat(capturedNotification).isNotNull();
        assertThat(capturedNotification.getStatus()).isEqualTo(NotificationRes.NotificationStatusRes.FAILED_TO_PROCESS);
    }

    @Test
    void whenProcessingSuccessThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        Long notificationId = 789L;
        NotificationRes notification = new NotificationRes();
        notification.setSequenceId(notificationId);

        when(restUtils.get(anyString(), isNull(), anyLong(), eq(NotificationRes.class)))
                .thenReturn(notification);
        when(restUtils.put(anyString(), isNull(), anyLong(), any(NotificationRes.class), eq(NotificationRes.class)))
                .thenReturn(notification);

        // When
        notificationClient.processingSuccess(notificationId);

        // Then
        verify(restUtils).put(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                any(NotificationRes.class),
                eq(NotificationRes.class)
        );
    }

    @Test
    void whenProcessingFailureThenCorrectEndpointIsUsed() throws ClientException {
        // Given
        Long notificationId = 101L;
        NotificationRes notification = new NotificationRes();
        notification.setSequenceId(notificationId);

        when(restUtils.get(anyString(), isNull(), anyLong(), eq(NotificationRes.class)))
                .thenReturn(notification);
        when(restUtils.put(anyString(), isNull(), anyLong(), any(NotificationRes.class), eq(NotificationRes.class)))
                .thenReturn(notification);

        // When
        notificationClient.processingFailure(notificationId);

        // Then
        verify(restUtils).put(
                eq(NOTIFICATION_SERVICE_BASE_URL + "/api/v2/pp/notification/notifications/{id}"),
                isNull(),
                eq(notificationId),
                any(NotificationRes.class),
                eq(NotificationRes.class)
        );
    }

    private Object createTestEvent() {
        // Create a simple test object that can be used for testing
        // Since we're now using Object type, we can use any object
        return new Object() {
            @Override
            public String toString() {
                return "test-event";
            }
        };
    }
}

