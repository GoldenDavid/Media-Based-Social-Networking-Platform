package com.socialnetwork.service.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreatePostRequest;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.UploadService;
import com.socialnetwork.service.profile.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private ProfileService profileService;
    @Mock
    private UploadService uploadService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ObjectMapper objectMapper;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(profileService, uploadService, postRepository, rabbitTemplate, objectMapper);
    }

    @Test
    void createPost_ShouldReturnPostDto() {
        // Arrange
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        CreatePostRequest request = new CreatePostRequest();
        request.setCaption("Test Caption");
        request.setBase64ImageString("data:image/png;base64,abc");

        Profile profile = new Profile();
        profile.setId(1);
        profile.setUsername("testuser");

        when(profileService.getProfileEntity(userPrincipal)).thenReturn(profile);
        when(uploadService.uploadImage(anyString())).thenReturn("http://image.url");

        // Act
        PostDto result = postService.createPost(userPrincipal, request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Caption", result.getCaption());
        assertEquals("http://image.url", result.getImageUrl());
        verify(postRepository).save(any(Post.class));
        verify(rabbitTemplate).convertAndSend(anyString(), any());
    }

    @Test
    void getPost_WhenPostExists_ShouldReturnPostDto() {
        // Arrange
        Post post = new Post();
        post.setId(1);
        post.setCaption("Test Post");
        
        Profile profile = new Profile();
        profile.setUsername("author");
        post.setCreatedBy(profile);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // Act
        PostDto result = postService.getPost(1);

        // Assert
        assertNotNull(result);
        assertEquals("Test Post", result.getCaption());
    }

    @Test
    void getPost_WhenPostDoesNotExist_ShouldThrowException() {
        // Arrange
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> postService.getPost(1));
    }
}
