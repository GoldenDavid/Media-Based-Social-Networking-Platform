package com.socialnetwork.controller.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.dto.BaseResponse;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.FollowUserRequest;
import com.socialnetwork.dto.profile.FollowUserResponse;
import com.socialnetwork.dto.profile.GetFollowerResponse;
import com.socialnetwork.dto.profile.GetFollowingResponse;
import com.socialnetwork.dto.profile.UnFollowUserResponse;
import com.socialnetwork.dto.profile.UnfollowUserRequest;
import com.socialnetwork.service.profile.FollowerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(path = "/follow")
@Validated
public class FollowerController {
  private final FollowerService followerService;

  public FollowerController(FollowerService followerService) {
    this.followerService = followerService;
  }

  @GetMapping("/user/followers/{id}")
  public ResponseEntity<BaseResponse<GetFollowerResponse>> getFollowers(@PathVariable int id,
      @RequestParam("page") @Min(1) Integer page,
      @RequestParam("limit") @Min(1) int limit) {
    log.info("userId={}, page={}, limit={}", id, page, limit);
    return ResponseEntity.ok().body(BaseResponse.ok(followerService.getFollowers(id, page, limit)));
  }

  @GetMapping("/user/followings/{id}")
  public ResponseEntity<BaseResponse<GetFollowingResponse>> getFollowing(@PathVariable int id, @RequestParam("page") @Min(1) int page,
      @RequestParam("limit") @Min(1) int limit) {
    log.info("userId={}, page={}, limit={}", id, page, limit);
    return ResponseEntity.ok().body(BaseResponse.ok(followerService.getFollowings(id, page, limit)));
  }

  @PostMapping()
  public ResponseEntity<BaseResponse<FollowUserResponse>> folowUser(
      @Valid @RequestBody FollowUserRequest request, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    followerService.folowUser(userPrincipal, request.getProfileId());
    return ResponseEntity.ok().body(BaseResponse.ok(null));
  }

  @DeleteMapping()
  public ResponseEntity<BaseResponse<UnFollowUserResponse>> unfolowUser(
      @Valid @RequestBody UnfollowUserRequest request, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    followerService.unfolowUser(userPrincipal, request.getProfileId());
    return ResponseEntity.ok().body(BaseResponse.ok(null));
  }
}
