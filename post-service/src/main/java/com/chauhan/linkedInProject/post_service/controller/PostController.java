package com.chauhan.linkedInProject.post_service.controller;

import com.chauhan.linkedInProject.post_service.auth.AuthContextHolder;
import com.chauhan.linkedInProject.post_service.dto.PostCreateRequestDto;
import com.chauhan.linkedInProject.post_service.dto.PostDto;
import com.chauhan.linkedInProject.post_service.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostCreateRequestDto postCreateRequestDto,
                                              HttpServletRequest httpServletRequest) {
        Long userId = AuthContextHolder.getCurrentUserId();
        PostDto postDto = postService.createPost(postCreateRequestDto, userId);
        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        Long userId = AuthContextHolder.getCurrentUserId();
        PostDto postDto = postService.getPostById(postId);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/users/{userId}/allPosts")
    public ResponseEntity<List<PostDto>> getAllPostsOfUser(@PathVariable Long userId) {
        List<PostDto> posts = postService.getAllPostsOfUser(userId);
        return ResponseEntity.ok(posts);
    }


}
