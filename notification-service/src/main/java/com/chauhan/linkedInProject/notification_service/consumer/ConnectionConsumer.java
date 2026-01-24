package com.chauhan.linkedInProject.notification_service.consumer;
import com.chauhan.linkedInProject.connection_service.event.ConnectionEvent;
import com.chauhan.linkedInProject.notification_service.entity.Notification;
import com.chauhan.linkedInProject.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "connection_requested_topic")
    public void handleConnectionRequested(ConnectionEvent connectionEvent) {
        log.info("handleConnectionRequested: {}", connectionEvent);
        Long senderId = connectionEvent.getSenderId();
        Long receiverId = connectionEvent.getReceiverId();
        String message = String.format("User with id: %d has requested connection request to you",senderId);
        Notification notification = Notification.builder()
                .message(message)
                .userId(receiverId)//receiver's info
                .build();
        notificationService.addNotification(notification);
    }

    @KafkaListener(topics = "connection_accepted_topic")
    public void handleConnectionAccepted(ConnectionEvent connectionEvent) {
        log.info("handleConnectionAccepted: {}", connectionEvent);
        Long senderId = connectionEvent.getSenderId();
        Long receiverId = connectionEvent.getReceiverId();
        String message = String.format("User with id: %d has accepted your connection request",receiverId);
        Notification notification = Notification.builder()
                .message(message)
                .userId(senderId)//sender's info
                .build();
        notificationService.addNotification(notification);
    }

}
