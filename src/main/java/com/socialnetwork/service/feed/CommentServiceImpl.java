package com.socialnetwork.service.feed;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.dto.PostDto;
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
import com.socialnetwork.util.MapperUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final ProfileService profileService;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  @Override
  @Transactional
  public PostDto createComment(UserPrincipal userPrincipal, CreateCommentRequest request) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);
    Comment comment = new Comment();
    comment.setComment(request.getComment());
    comment.setCreatedAt(new Date());
    comment.setCreatedBy(profile);
    comment.setPost(post);
    commentRepository.save(comment);
    return MapperUtils.toDto(post);
  }

  @Override
  @Transactional
  public PostDto deleteComment(UserPrincipal userPrincipal, int commentId) {
    Profile profile = profileService.getProfileEntity(userPrincipal);
    Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    if (comment.getCreatedBy().getId() != profile.getId()) {
      throw new NoPermissionException();
    }
    Post post = comment.getPost();
    commentRepository.delete(comment);
    return MapperUtils.toDto(post);
  }

}
