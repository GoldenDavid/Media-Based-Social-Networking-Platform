package com.socialnetwork.service.feed;

import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.post.dto.feed.CreateCommentRequest;

public interface CommentService {
  PostDto createComment(UserPrincipal userPrincipal, CreateCommentRequest request);

  PostDto deleteComment(UserPrincipal userPrincipal, int commentId);
}
