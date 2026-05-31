package com.socialnetwork.post.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.post.dto.BaseResponse;
import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.dto.CreatePostResponse;
import com.socialnetwork.post.dto.DeletePostResponse;
import com.socialnetwork.post.dto.GetPostResponse;
import com.socialnetwork.post.dto.GetUserPostResponse;
import com.socialnetwork.post.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/posts")
public class PostController {
  private final PostService postService;

  @PostMapping()
  public ResponseEntity<BaseResponse<CreatePostResponse>> createPost(
      @Valid @RequestBody CreatePostRequest request, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostDto post = postService.createPost(userPrincipal, request);
    return ResponseEntity.ok().body(BaseResponse.ok(CreatePostResponse.builder().post(post).build()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<GetPostResponse>> getPost(@PathVariable int id) {
    PostDto post = postService.getPost(id);
    return ResponseEntity.ok().body(BaseResponse.ok(GetPostResponse.builder().post(post).build()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<DeletePostResponse> deletePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    postService.deletePost(userPrincipal, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/like/{id}")
  public ResponseEntity<BaseResponse<GetPostResponse>> likePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostDto post = postService.likePost(userPrincipal, id);
    return ResponseEntity.ok().body(BaseResponse.ok(GetPostResponse.builder().post(post).build()));
  }

  @DeleteMapping("/like/{id}")
  public ResponseEntity<BaseResponse<GetPostResponse>> unlikePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostDto post = postService.unlikePost(userPrincipal, id);
    return ResponseEntity.ok().body(BaseResponse.ok(GetPostResponse.builder().post(post).build()));
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<BaseResponse<GetUserPostResponse>> getUserPosts(@PathVariable int id) {
    List<PostDto> posts = postService.getUserPosts(id);
    return ResponseEntity.ok().body(BaseResponse.ok(GetUserPostResponse.builder().posts(posts).build()));
  }
}
