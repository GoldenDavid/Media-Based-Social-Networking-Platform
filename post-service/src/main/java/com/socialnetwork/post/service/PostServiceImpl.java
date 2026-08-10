package com.socialnetwork.post.service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.socialnetwork.common.event.NotificationEvent;
import com.socialnetwork.common.event.NotificationType;
import com.socialnetwork.post.config.MessageQueueConfig;
import com.socialnetwork.post.dto.CommentDto;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.exception.NoPermissionException;
import com.socialnetwork.post.exception.PostNotFoundException;
import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.repository.PostRepository;
import com.socialnetwork.post.util.MapperUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final ProfileService profileService;
  private final UploadService uploadService;
  private final PostRepository postRepository;
  private final RabbitTemplate rabbitTemplate;

  @Override
  @Transactional
  public PostDto createPost(UserPrincipal userPrincipal, CreatePostRequest request) {
    ProfileDto profile = profileService.getProfile(userPrincipal);
    String url = uploadService.uploadImage(request.getBase64ImageString());
    if (url == null || url.isBlank()) {
      throw new com.socialnetwork.post.exception.InvalidInputException("Image upload failed");
    }

    Post post = new Post();
    post.setCaption(request.getCaption());
    post.setCreatedAt(new Date());
    post.setCreatedByProfileId(profile.getId());
    post.setImageUrl(url);
    postRepository.save(post);
    log.info("Created post with id: {} by profileId: {}", post.getId(), profile.getId());

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        rabbitTemplate.convertAndSend(MessageQueueConfig.AFTER_CREATE_POST_QUEUE, post.getId());

        // Fan-out a NEW_POST notification to every follower of the post author.
        // Failures are logged, not thrown, so a misbehaving follower fetch does not
        // roll back the post creation.
        try {
          List<Integer> followerIds = profileService.getFollowerIds(profile.getId());
          for (Integer followerId : followerIds) {
            NotificationEvent event = NotificationEvent.builder()
                .type(NotificationType.NEW_POST)
                .fromProfileId(profile.getId())
                .toProfileId(followerId)
                .postId(post.getId())
                .build();
            rabbitTemplate.convertAndSend(MessageQueueConfig.NOTIFICATION_EVENT_QUEUE, event);
          }
          log.info("Fanned out NEW_POST notification to {} follower(s) of profileId={}",
              followerIds.size(), profile.getId());
        } catch (Exception ex) {
          log.warn("Failed to fan out NEW_POST notifications for postId={}: {}",
              post.getId(), ex.getMessage());
        }
      }
    });

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
    ProfileDto profile = profileService.getProfile(userPrincipal);
    Post post = getPostEntity(postId);
    if (post.getCreatedByProfileId() != profile.getId()) {
      log.warn("profileId={} tried to delete post={} without permission", profile.getId(), postId);
      throw new NoPermissionException();
    }
    postRepository.delete(post);
    log.info("Deleted post with id: {}", postId);
  }

  @Override
  @Transactional
  public PostDto likePost(UserPrincipal userPrincipal, int postId) {
    ProfileDto profile = profileService.getProfile(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikesProfileIds().add(profile.getId());
    postRepository.save(post);
    log.info("profileId={} liked post={}", profile.getId(), postId);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        if (post.getCreatedByProfileId() != profile.getId()) {
          try {
            NotificationEvent event = NotificationEvent.builder()
                .type(NotificationType.LIKE_YOUR_POST)
                .fromProfileId(profile.getId())
                .toProfileId(post.getCreatedByProfileId())
                .postId(post.getId())
                .build();
            rabbitTemplate.convertAndSend(MessageQueueConfig.NOTIFICATION_EVENT_QUEUE, event);
          } catch (Exception ex) {
            log.warn("Failed to publish LIKE_YOUR_POST for postId={}: {}", post.getId(), ex.getMessage());
          }
        }
      }
    });

    return toPostDto(post);
  }

  @Override
  @Transactional
  public PostDto unlikePost(UserPrincipal userPrincipal, int postId) {
    ProfileDto profile = profileService.getProfile(userPrincipal);
    Post post = getPostEntity(postId);
    post.getUserLikesProfileIds().remove(profile.getId());
    postRepository.save(post);
    return toPostDto(post);
  }

  @Override
  public List<PostDto> getUserPosts(int profileId) {
    return postRepository.findByCreatedByProfileId(profileId)
        .stream().map(this::toPostDto).collect(Collectors.toList());
  }

  // ── Private helper ────────────────────────────────────────────────────────

  private PostDto toPostDto(Post post) {
    ProfileDto author = profileService.getUserProfile(post.getCreatedByProfileId());

    List<CommentDto> comments = Collections.emptyList();
    if (post.getComments() != null) {
      comments = post.getComments().stream()
          .map(c -> MapperUtils.toDto(c, profileService.getUserProfile(c.getCreatedByProfileId())))
          .collect(Collectors.toList());
    }

    Set<ProfileDto> likes = Collections.emptySet();
    if (post.getUserLikesProfileIds() != null) {
      likes = post.getUserLikesProfileIds().stream()
          .map(id -> profileService.getUserProfile(id))
          .collect(Collectors.toSet());
    }

    return MapperUtils.toDto(post, author, comments, likes);
  }
}
