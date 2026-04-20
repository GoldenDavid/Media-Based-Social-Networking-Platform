package com.socialnetwork.service.feed;

import java.util.List;

import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreatePostRequest;
import com.socialnetwork.model.Post;

public interface PostService {
  PostDto createPost(UserPrincipal userPrincipal, CreatePostRequest request);

  PostDto getPost(int postId);

  Post getPostEntity(int postId);

  void deletePost(UserPrincipal userPrincipal, int postId);

  PostDto likePost(UserPrincipal userPrincipal, int postId);

  PostDto unlikePost(UserPrincipal userPrincipal, int postId);

  List<PostDto> getUserPosts(int userId);
}
