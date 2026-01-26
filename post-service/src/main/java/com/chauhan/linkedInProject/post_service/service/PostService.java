package com.chauhan.linkedInProject.post_service.service;

import com.chauhan.linkedInProject.post_service.auth.AuthContextHolder;
import com.chauhan.linkedInProject.post_service.client.ConnectionsServiceClient;
import com.chauhan.linkedInProject.post_service.client.UploaderServiceClient;
import com.chauhan.linkedInProject.post_service.dto.PersonDto;
import com.chauhan.linkedInProject.post_service.dto.PostCreateRequestDto;
import com.chauhan.linkedInProject.post_service.dto.PostDto;
import com.chauhan.linkedInProject.post_service.entity.Post;
import com.chauhan.linkedInProject.post_service.event.PostCreated;
import com.chauhan.linkedInProject.post_service.exception.ResourceNotFoundException;
import com.chauhan.linkedInProject.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsServiceClient connectionsServiceClient;
    private final KafkaTemplate<Long, PostCreated> postCreatedKafkaTemplate;
    private final UploaderServiceClient uploaderServiceClient;

    public PostDto createPost(
            @RequestPart(
                    value = "post",
                    required = true
            ) PostCreateRequestDto postCreateRequestDto,

            @RequestPart(
                    value = "file",
                    required = true
            ) MultipartFile file
    ){
        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("Creating post for user with id: {}", userId);

        //get the url of img/vid when you upload it first
        String url = uploaderServiceClient.uploadFile(file);
        log.info("url of img/vid: {}", url);

        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);
        post.setUrl(url);
        post = postRepository.save(post);

        List<PersonDto> personDtoList = connectionsServiceClient.getFirstDegreeConnections(userId);

        for(PersonDto person: personDtoList) { // sending notification to each connection
            PostCreated postCreated = PostCreated.builder()
                    .postId(post.getId())
                    .content(post.getContent())
                    .userId(person.getUserId())
                    .ownerUserId(userId)
                    .build();
            postCreatedKafkaTemplate.send("post_created_topic", postCreated);
        }

        return modelMapper.map(post, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.info("Getting the post with ID: {}", postId);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found " +
                "with ID: "+postId));

        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        log.info("Getting all the posts of a user with ID: {}", userId);
        List<Post> postList = postRepository.findByUserId(userId);

        return postList
                .stream()
                .map((element) -> modelMapper.map(element, PostDto.class))
                .collect(Collectors.toList());
    }
}
