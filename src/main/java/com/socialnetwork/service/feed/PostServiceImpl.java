package com.socialnetwork.service.feed;

import java.util.Date;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.config.MessageQueueConfig;
import com.socialnetwork.dto.PostDto;
import com.socialnetwork.exception.NoPermissionException;
import com.socialnetwork.exception.PostNotFoundException;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.UploadService;
import com.socialnetwork.service.profile.ProfileService;
import com.socialnetwork.util.MapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
  private final ProfileService profileService;
  private final UploadService uploadService;
  private final PostRepository postRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public PostServiceImpl(ProfileService profileService, UploadService uploadService, PostRepository postRepository,
      RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
    this.profileService = profileService;
    this.uploadService = uploadService;
    this.postRepository = postRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public PostDto createPost(UserPrincipal userPrincipal, CreatePostRequest request) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    String url = uploadService.uploadImage(request.getBase64ImageString());
    Post post = new Post();
    post.setCaption(request.getCaption());
    post.setCreatedAt(new Date());
    post.setCreatedBy(profile);
    post.setImageUrl(url);
    postRepository.save(post);
    log.info("Created post with id: {} by user: {}", post.getId(), profile.getUsername());

    rabbitTemplate.convertAndSend(MessageQueueConfig.AFTER_CREATE_POST_QUEUE, post.getId());

    return MapperUtils.toDto(post);
  }

  @Override
  public PostDto getPost(int postId) {
    Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
    return MapperUtils.toDto(post);
  }

  public Post getPostEntity(int postId) {
    return postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
  }

  @Override
  @Transactional
  public void deletePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Post post = getPostEntity(postId);
    if (post.getCreatedBy().getId() != profile.getId()) {
      log.warn("User {} tried to delete post {} without permission", profile.getUsername(), postId);
      throw new NoPermissionException();
    }
    postRepository.delete(post);
    log.info("Deleted post with id: {}", postId);
  }

  @Override
  @Transactional
  public PostDto likePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikes().add(profile);
    postRepository.save(post);
    log.info("User {} liked post {}", profile.getUsername(), postId);

    return MapperUtils.toDto(post);
  }

  @Override
  @Transactional
  public PostDto unlikePost(UserPrincipal userPrincipal, int postId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikes().remove(profile);
    postRepository.save(post);
    return MapperUtils.toDto(post);
  }

  @Override
  public List<PostDto> getUserPosts(int userId) {
    Profile profile = profileService.getUserProfile(userId);
    return postRepository.findByCreatedBy(profile).stream().map(MapperUtils::toDto).collect(Collectors.toList());
  }

}
