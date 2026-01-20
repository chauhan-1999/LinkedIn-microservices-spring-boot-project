package com.chauhan.linkedInProject.post_service.event;

import lombok.Data;

@Data
public class PostLiked {
    private Long postId;
    private Long ownerUserId;
    private Long likedByUserId;
}
