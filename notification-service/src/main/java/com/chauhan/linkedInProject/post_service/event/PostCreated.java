package com.chauhan.linkedInProject.post_service.event;

import lombok.Data;

@Data
public class PostCreated {
    private Long ownerUserId;
    private Long postId;
    private Long userId;
    private String content;
}
