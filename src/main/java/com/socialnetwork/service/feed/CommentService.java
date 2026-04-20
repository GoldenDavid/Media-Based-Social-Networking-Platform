package com.socialnetwork.service.feed;

import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreateCommentRequest;

public interface CommentService {
  PostDto createComment(UserPrincipal userPrincipal, CreateCommentRequest request);

  PostDto deleteComment(UserPrincipal userPrincipal, int commentId);
}
