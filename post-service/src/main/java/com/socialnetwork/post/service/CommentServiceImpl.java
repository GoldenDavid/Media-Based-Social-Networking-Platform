package com.socialnetwork.post.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.CreateCommentRequest;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.UserPrincipal;
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
