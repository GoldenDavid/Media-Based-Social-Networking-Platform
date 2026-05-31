package com.socialnetwork.post.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.post.config.MessageQueueConfig;
import com.socialnetwork.post.dto.CommentDto;
import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.exception.NoPermissionException;
import com.socialnetwork.post.exception.PostNotFoundException;
import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.model.Profile;
import com.socialnetwork.post.repository.PostRepository;
import com.socialnetwork.post.util.MapperUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
  private final ProfileService profileService;
  private final UploadService uploadService;
  private final PostRepository postRepository;
  private final RabbitTemplate rabbitTemplate;

  public PostServiceImpl(ProfileService profileService, UploadService uploadService, PostRepository postRepository,
      RabbitTemplate rabbitTemplate) {
    this.profileService = profileService;
    this.uploadService = uploadService;
    this.postRepository = postRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  @Transactional
  public PostDto createPost(UserPrincipal userPrincipal, CreatePostRequest request) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    String url = uploadService.uploadImage(request.getBase64ImageString());
    Post post = new Post();
    post.setCaption(request.getCaption());
    post.setCreatedAt(new Date());
    post.setCreatedByProfileId(profile.getId());
    post.setImageUrl(url);
    postRepository.save(post);
    log.info("Created post with id: {} by user: {}", post.getId(), profile.getUsername());

    rabbitTemplate.convertAndSend(MessageQueueConfig.AFTER_CREATE_POST_QUEUE, post.getId());

    return toPostDto(post);
  }

  @Override
  public PostDto getPost(int postId) {
    Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
    return toPostDto(post);
  }

  @Override
  public Post getPostEntity(int postId) {
    return postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
  }

  @Override
  @Transactional
  public void deletePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    Post post = getPostEntity(postId);
    if (post.getCreatedByProfileId() != profile.getId()) {
      log.warn("User {} tried to delete post {} without permission", profile.getUsername(), postId);
      throw new NoPermissionException();
    }
    postRepository.delete(post);
    log.info("Deleted post with id: {}", postId);
  }

  @Override
  @Transactional
  public PostDto likePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikesProfileIds().add(profile.getId());
    postRepository.save(post);
    log.info("User {} liked post {}", profile.getUsername(), postId);

    return toPostDto(post);
  }

  @Override
  @Transactional
  public PostDto unlikePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikesProfileIds().remove(profile.getId());
    postRepository.save(post);
    return toPostDto(post);
  }

  @Override
  public List<PostDto> getUserPosts(int userId) {
    Profile profile = profileService.getProfileEntity(userId);
    return postRepository.findByCreatedByProfileId(profile.getId()).stream().map(this::toPostDto).collect(Collectors.toList());
  }

  private PostDto toPostDto(Post post) {
      ProfileDto author = profileService.getUserProfile(post.getCreatedByProfileId());
      
      List<CommentDto> comments = java.util.Collections.emptyList();
      if (post.getComments() != null) {
          comments = post.getComments().stream()
              .map(c -> MapperUtils.toDto(c, profileService.getUserProfile(c.getCreatedByProfileId())))
              .collect(Collectors.toList());
      }
      
      Set<ProfileDto> likes = java.util.Collections.emptySet();
      if (post.getUserLikesProfileIds() != null) {
          likes = post.getUserLikesProfileIds().stream()
              .map(profileService::getUserProfile)
              .collect(Collectors.toSet());
      }
      
      return MapperUtils.toDto(post, author, comments, likes);
  }

}
