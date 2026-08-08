package com.socialnetwork.profile.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.common.dto.BaseResponse;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.profile.dto.ProfileDto;
import com.socialnetwork.profile.dto.UpdateProfileImageRequest;
import com.socialnetwork.profile.dto.UpdateProfileRequest;
import com.socialnetwork.profile.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<Map<String, ProfileDto>>> getMyProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.getUserProfile(principal);
        return ResponseEntity.ok(BaseResponse.ok(Map.of("profile", profile)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<Map<String, ProfileDto>>> getProfile(@PathVariable int id) {
        ProfileDto profile = profileService.getUserProfile(id);
        return ResponseEntity.ok(BaseResponse.ok(Map.of("profile", profile)));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Map<String, List<ProfileDto>>>> searchProfiles(@RequestParam String query) {
        List<ProfileDto> profiles = profileService.searchProfiles(query);
        return ResponseEntity.ok(BaseResponse.ok(Map.of("profiles", profiles)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Map<String, ProfileDto>>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.updateProfile(principal, request);
        return ResponseEntity.ok(BaseResponse.ok(Map.of("profile", profile)));
    }

    @PostMapping("/profile-image")
    public ResponseEntity<BaseResponse<Map<String, String>>> updateProfileImage(
            @Valid @RequestBody UpdateProfileImageRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.updateProfileImage(principal, request);
        return ResponseEntity.ok(BaseResponse.ok(Map.of("url", profile.getProfileImageUrl())));
    }
}
