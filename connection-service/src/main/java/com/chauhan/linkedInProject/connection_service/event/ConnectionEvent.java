package com.chauhan.linkedInProject.connection_service.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionEvent {
    private Long senderId;
    private Long receiverId;
}
