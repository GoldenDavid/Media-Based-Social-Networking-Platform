package com.socialnetwork.controller.feed;

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

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreatePostRequest;
import com.socialnetwork.dto.feed.CreatePostResponse;
import com.socialnetwork.dto.feed.DeletePostResponse;
import com.socialnetwork.dto.feed.GetPostResponse;
import com.socialnetwork.dto.feed.GetUserPostResponse;
import com.socialnetwork.model.Post;
import com.socialnetwork.service.feed.PostService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(path = "/posts")
public class PostController {
  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping()
  public ResponseEntity<CreatePostResponse> createPost(
      @Valid @RequestBody CreatePostRequest request, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Post post = postService.createPost(userPrincipal, request);
    return ResponseEntity.ok().body(CreatePostResponse.builder().post(post).build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<GetPostResponse> getPost(@PathVariable int id) {
    Post post = postService.getPost(id);
    return ResponseEntity.ok().body(GetPostResponse.builder().post(post).build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<DeletePostResponse> deletePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    postService.deletePost(userPrincipal, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/like/{id}")
  public ResponseEntity<GetPostResponse> likePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Post post = postService.likePost(userPrincipal, id);
    return ResponseEntity.ok().body(GetPostResponse.builder().post(post).build());
  }

  @DeleteMapping("/like/{id}")
  public ResponseEntity<GetPostResponse> unlikePost(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Post post = postService.unlikePost(userPrincipal, id);
    return ResponseEntity.ok().body(GetPostResponse.builder().post(post).build());
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<GetUserPostResponse> getUserPosts(@PathVariable int id) {
    List<Post> posts = postService.getUserPosts(id);
    return ResponseEntity.ok().body(GetUserPostResponse.builder().posts(posts).build());
  }
}
