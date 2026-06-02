package com.socialnetwork.profile.controller;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.profile.dto.FollowUserRequest;
import com.socialnetwork.profile.dto.ProfileDto;
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

import java.util.Map;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/follow")
@Validated
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerService followerService;

    @GetMapping("/user/followers/{id}")
    public ResponseEntity<?> getFollowers(
            @PathVariable int id,
            @RequestParam("page") @Min(1) Integer page,
            @RequestParam("limit") @Min(1) int limit) {
        
        log.info("getFollowers: userId={}, page={}, limit={}", id, page, limit);
        List<ProfileDto> followers = followerService.getFollowers(id, page, limit);
        int totalFollowers = followerService.countFollowers(id);
        int totalPage = (int) Math.ceil((double) totalFollowers / limit);
        
        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "totalPage", totalPage,
                        "followers", followers
                )
        ));
    }

    @GetMapping("/user/followings/{id}")
    public ResponseEntity<?> getFollowing(
            @PathVariable int id,
            @RequestParam("page") @Min(1) int page,
            @RequestParam("limit") @Min(1) int limit) {
            
        log.info("getFollowings: userId={}, page={}, limit={}", id, page, limit);
        List<ProfileDto> followings = followerService.getFollowings(id, page, limit);
        int totalFollowings = followerService.countFollowings(id);
        int totalPage = (int) Math.ceil((double) totalFollowings / limit);
        
        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "totalPage", totalPage,
                        "followings", followings
                )
        ));
    }

    @PostMapping
    public ResponseEntity<?> folowUser(
            @Valid @RequestBody FollowUserRequest request, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        followerService.followUser(userPrincipal, request.getProfileId());
        return ResponseEntity.ok(Map.of("data", Map.of()));
    }

    @DeleteMapping
    public ResponseEntity<?> unfolowUser(
            @Valid @RequestBody UnfollowUserRequest request, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        followerService.unfollowUser(userPrincipal, request.getProfileId());
        return ResponseEntity.ok(Map.of("data", Map.of()));
    }
}
