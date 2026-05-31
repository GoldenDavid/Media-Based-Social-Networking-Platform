package com.socialnetwork.profile.controller;

import com.socialnetwork.profile.dto.*;
import com.socialnetwork.profile.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for profile CRUD — client-facing API.
 * Internal service-to-service calls use the gRPC ProfileGrpcService instead.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.getUserProfile(principal);
        return ResponseEntity.ok(Map.of("profile", profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable int id) {
        ProfileDto profile = profileService.getUserProfile(id);
        return ResponseEntity.ok(Map.of("profile", profile));
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.updateProfile(principal, request);
        return ResponseEntity.ok(Map.of("profile", profile));
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> updateProfileImage(
            @Valid @RequestBody UpdateProfileImageRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ProfileDto profile = profileService.updateProfileImage(principal, request);
        return ResponseEntity.ok(Map.of("url", profile.getProfileImageUrl()));
    }
}
