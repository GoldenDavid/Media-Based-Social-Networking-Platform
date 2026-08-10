package com.socialnetwork.post.service;

import java.util.Date;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.socialnetwork.common.event.NotificationEvent;
import com.socialnetwork.common.event.NotificationType;
import com.socialnetwork.post.config.MessageQueueConfig;
import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.CreateCommentRequest;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.exception.CommentNotFoundException;
import com.socialnetwork.post.exception.NoPermissionException;
import com.socialnetwork.post.exception.PostNotFoundException;
import com.socialnetwork.post.model.Comment;
import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.repository.CommentRepository;
import com.socialnetwork.post.repository.PostRepository;
import com.socialnetwork.post.util.MapperUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final ProfileService profileService;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final RabbitTemplate rabbitTemplate;

  @Override
  @Transactional
  public PostDto createComment(UserPrincipal userPrincipal, CreateCommentRequest request) {
    ProfileDto profile = profileService.getProfile(userPrincipal);
    Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

    Comment comment = new Comment();
    comment.setComment(request.getComment());
    comment.setCreatedAt(new Date());
    comment.setCreatedByProfileId(profile.getId());
    comment.setPost(post);
    commentRepository.save(comment);

    log.info("profileId={} commented on post={}", profile.getId(), post.getId());

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        if (post.getCreatedByProfileId() != profile.getId()) {
          try {
            NotificationEvent event = NotificationEvent.builder()
                .type(NotificationType.COMMENT_YOUR_POST)
                .fromProfileId(profile.getId())
                .toProfileId(post.getCreatedByProfileId())
                .postId(post.getId())
                .build();
            rabbitTemplate.convertAndSend(MessageQueueConfig.NOTIFICATION_EVENT_QUEUE, event);
          } catch (Exception ex) {
            log.warn("Failed to publish COMMENT_YOUR_POST for postId={}: {}",
                post.getId(), ex.getMessage());
          }
        }
      }
    });

    // Return a minimal PostDto (full hydration is handled by PostServiceImpl.toPostDto)
    return MapperUtils.toDto(post, profileService.getUserProfile(post.getCreatedByProfileId()),
        java.util.Collections.emptyList(), java.util.Collections.emptySet());
  }

  @Override
  @Transactional
  public PostDto deleteComment(UserPrincipal userPrincipal, int commentId) {
    ProfileDto profile = profileService.getProfile(userPrincipal);
    Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

    if (comment.getCreatedByProfileId() != profile.getId()) {
      throw new NoPermissionException();
    }

    Post post = comment.getPost();
    commentRepository.delete(comment);

    log.info("profileId={} deleted comment={}", profile.getId(), commentId);
    return MapperUtils.toDto(post, profileService.getUserProfile(post.getCreatedByProfileId()),
        java.util.Collections.emptyList(), java.util.Collections.emptySet());
  }
}
