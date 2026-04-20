package com.socialnetwork.service.feed;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreateCommentRequest;
import com.socialnetwork.model.Post;

public interface CommentService {
  Post createComment(UserPrincipal userPrincipal, CreateCommentRequest request);

  Post deleteComment(UserPrincipal userPrincipal, int commentId);
}
