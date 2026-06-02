package com.socialnetwork.profile.controller;

import com.socialnetwork.common.dto.BaseResponse;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.profile.dto.FollowResponse;
import com.socialnetwork.profile.dto.FollowUserRequest;
import com.socialnetwork.profile.dto.FollowersResponse;
import com.socialnetwork.profile.dto.FollowingsResponse;
import com.socialnetwork.profile.dto.UnfollowUserRequest;
import com.socialnetwork.profile.service.FollowerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/follow")
@Validated
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerService followerService;

    @GetMapping("/user/followers/{id}")
    public ResponseEntity<BaseResponse<FollowersResponse>> getFollowers(
            @PathVariable int id,
            @RequestParam("page") @Min(1) Integer page,
            @RequestParam("limit") @Min(1) int limit) {

        log.info("getFollowers: userId={}, page={}, limit={}", id, page, limit);
        var followers = followerService.getFollowers(id, page, limit);
        int totalCount = followerService.countFollowers(id);
        int totalPage = (int) Math.ceil((double) totalCount / limit);
        var body = FollowersResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .followers(followers)
                .build();
        return ResponseEntity.ok(BaseResponse.ok(body));
    }

    @GetMapping("/user/followings/{id}")
    public ResponseEntity<BaseResponse<FollowingsResponse>> getFollowings(
            @PathVariable int id,
            @RequestParam("page") @Min(1) Integer page,
            @RequestParam("limit") @Min(1) int limit) {

        log.info("getFollowings: userId={}, page={}, limit={}", id, page, limit);
        var followings = followerService.getFollowings(id, page, limit);
        int totalCount = followerService.countFollowings(id);
        int totalPage = (int) Math.ceil((double) totalCount / limit);
        var body = FollowingsResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .followings(followings)
                .build();
        return ResponseEntity.ok(BaseResponse.ok(body));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<FollowResponse>> followUser(
            @Valid @RequestBody FollowUserRequest request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        followerService.followUser(userPrincipal, request.getProfileId());
        var body = FollowResponse.builder()
                .followed(true)
                .profileId(request.getProfileId())
                .build();
        return ResponseEntity.ok(BaseResponse.ok(body));
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<FollowResponse>> unfollowUser(
            @Valid @RequestBody UnfollowUserRequest request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        followerService.unfollowUser(userPrincipal, request.getProfileId());
        var body = FollowResponse.builder()
                .followed(false)
                .profileId(request.getProfileId())
                .build();
        return ResponseEntity.ok(BaseResponse.ok(body));
    }
}
