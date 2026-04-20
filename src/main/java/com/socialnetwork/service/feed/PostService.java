package com.socialnetwork.service.feed;

import java.util.List;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreatePostRequest;
import com.socialnetwork.model.Post;

public interface PostService {
  Post createPost(UserPrincipal userPrincipal, CreatePostRequest request);

  Post getPost(int postId);

  void deletePost(UserPrincipal userPrincipal, int postId);

  Post likePost(UserPrincipal userPrincipal, int postId);

  Post unlikePost(UserPrincipal userPrincipal, int postId);

  List<Post> getUserPosts(int userId);
}
