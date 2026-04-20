package com.socialnetwork.controller.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.dto.BaseResponse;
import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.CreateCommentRequest;
import com.socialnetwork.dto.feed.GetPostResponse;
import com.socialnetwork.service.feed.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
public class CommentController {
  private final CommentService commentService;

  @PostMapping()
  public ResponseEntity<BaseResponse<GetPostResponse>> createComment(
      @Valid @RequestBody CreateCommentRequest request, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostDto post = commentService.createComment(userPrincipal, request);
    return ResponseEntity.ok().body(BaseResponse.ok(GetPostResponse.builder().post(post).build()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<GetPostResponse>> deleteComment(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    PostDto post = commentService.deleteComment(userPrincipal, id);
    return ResponseEntity.ok().body(BaseResponse.ok(GetPostResponse.builder().post(post).build()));
  }
}
