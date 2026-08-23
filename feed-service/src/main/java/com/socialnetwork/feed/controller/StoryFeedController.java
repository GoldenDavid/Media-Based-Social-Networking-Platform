package com.socialnetwork.feed.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.feed.dto.ProfileDto;
import com.socialnetwork.feed.dto.StoryDto;
import com.socialnetwork.feed.dto.StoryFeedDto;
import com.socialnetwork.feed.grpc.PostServiceGrpcClient;
import com.socialnetwork.feed.grpc.ProfileServiceGrpcClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/story-feeds")
@RequiredArgsConstructor
public class StoryFeedController {

    private final ProfileServiceGrpcClient profileServiceGrpcClient;
    private final PostServiceGrpcClient postServiceGrpcClient;

    @GetMapping
    public ResponseEntity<?> getStoryFeeds(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        // 1. Get profile ID of current user
        ProfileDto currentProfile = profileServiceGrpcClient.getProfileByUserId(
                userPrincipal.getId().toString(), 
                userPrincipal.getName());
        
        // 2. Get followings
        List<Integer> followingIds = profileServiceGrpcClient.getFollowingIds(currentProfile.getId());
        
        // 3. Add own profile ID to also see own stories
        List<Integer> targetProfileIds = new ArrayList<>(followingIds);
        targetProfileIds.add(currentProfile.getId());

        // 4. Fetch active stories (within 24h) for all target profiles
        List<StoryDto> activeStories = postServiceGrpcClient.getActiveStories(targetProfileIds);

        // 5. Group by author
        Map<Integer, List<StoryDto>> storiesByAuthor = new HashMap<>();
        Map<Integer, ProfileDto> authorsById = new HashMap<>();

        for (StoryDto story : activeStories) {
            ProfileDto author = story.getCreatedBy();
            if (author != null) {
                storiesByAuthor.computeIfAbsent(author.getId(), k -> new ArrayList<>()).add(story);
                authorsById.putIfAbsent(author.getId(), author);
            }
        }

        List<StoryFeedDto> storyFeeds = storiesByAuthor.entrySet().stream()
                .map(entry -> StoryFeedDto.builder()
                        .author(authorsById.get(entry.getKey()))
                        .stories(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Story feeds retrieved successfully",
            "timestamp", java.time.Instant.now().toString(),
            "data", Map.of("storyFeeds", storyFeeds)
        ));
    }
}
