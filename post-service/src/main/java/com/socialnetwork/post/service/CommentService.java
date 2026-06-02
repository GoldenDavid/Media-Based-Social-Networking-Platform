package com.socialnetwork.post.service;

import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.dto.CreateCommentRequest;

public interface CommentService {
  PostDto createComment(UserPrincipal userPrincipal, CreateCommentRequest request);

  PostDto deleteComment(UserPrincipal userPrincipal, int commentId);
}
