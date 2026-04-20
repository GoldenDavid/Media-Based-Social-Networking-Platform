package com.socialnetwork.service.feed;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreateCommentRequest;
import com.socialnetwork.exception.CommentNotFoundException;
import com.socialnetwork.exception.NoPermissionException;
import com.socialnetwork.exception.PostNotFoundException;
import com.socialnetwork.model.Comment;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.CommentRepository;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.profile.ProfileService;

@Service
public class CommentServiceImpl implements CommentService {
  private final ProfileService profileService;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  public CommentServiceImpl(ProfileService profileService, PostRepository postRepository,
      CommentRepository commentRepository) {
    this.profileService = profileService;
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
  }

  @Override
  @Transactional
  public Post createComment(UserPrincipal userPrincipal, CreateCommentRequest request) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);
    Comment comment = new Comment();
    comment.setComment(request.getComment());
    comment.setCreatedAt(new Date());
    comment.setCreatedBy(profile);
    comment.setPost(post);
    commentRepository.save(comment);
    return post;
  }

  @Override
  @Transactional
  public Post deleteComment(UserPrincipal userPrincipal, int commentId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    if (comment.getCreatedBy().getId() != profile.getId()) {
      throw new NoPermissionException();
    }
    commentRepository.delete(comment);
    return comment.getPost();
  }

}
