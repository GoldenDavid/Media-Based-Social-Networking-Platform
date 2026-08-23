package com.socialnetwork.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.dto.StoryDto;
import com.socialnetwork.post.service.StoryService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    public ResponseEntity<?> createStory(
            @RequestBody CreatePostRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        StoryDto story = storyService.createStory(userPrincipal, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Story created successfully",
                "timestamp", java.time.Instant.now().toString(),
                "data", Map.of("story", story)
        ));
    }
}
