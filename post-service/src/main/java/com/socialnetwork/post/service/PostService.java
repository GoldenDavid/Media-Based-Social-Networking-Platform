package com.socialnetwork.post.service;

import java.util.List;

import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.model.Post;

public interface PostService {
  PostDto createPost(UserPrincipal userPrincipal, CreatePostRequest request);

  PostDto getPost(int postId);

  Post getPostEntity(int postId);

  void deletePost(UserPrincipal userPrincipal, int postId);

  PostDto likePost(UserPrincipal userPrincipal, int postId);

  PostDto unlikePost(UserPrincipal userPrincipal, int postId);

  List<PostDto> getUserPosts(int userId);
}
