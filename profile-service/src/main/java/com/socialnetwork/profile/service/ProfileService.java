package com.socialnetwork.profile.service;

import com.socialnetwork.grpc.media.MediaServiceGrpc;
import com.socialnetwork.grpc.media.UploadImageRequest;
import com.socialnetwork.grpc.media.UploadImageResponse;
import com.socialnetwork.profile.dto.ProfileDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.profile.dto.UpdateProfileImageRequest;
import com.socialnetwork.profile.dto.UpdateProfileRequest;
import com.socialnetwork.profile.model.Profile;
import com.socialnetwork.profile.repository.ProfileRepository;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * REST-facing service for profile operations.
 * Image uploads delegate to media-service via gRPC stub.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @GrpcClient("media-service")
    private MediaServiceGrpc.MediaServiceBlockingStub mediaServiceStub;

    // ── Public API ────────────────────────────────────────────────────────────

    public ProfileDto getUserProfile(UserPrincipal userPrincipal) {
        return toDto(getOrCreateProfile(userPrincipal));
    }

    public ProfileDto getUserProfile(int id) {
        return toDto(getProfileById(id));
    }

    @Transactional
    public ProfileDto updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
        Profile profile = getOrCreateProfile(userPrincipal);
        profile.setBio(request.getBio());
        profile.setDisplayName(request.getDisplayName());
        profile.setUsername(request.getUsername());
        profileRepository.save(profile);
        log.info("Updated profile for user: {}", profile.getUsername());
        return toDto(profile);
    }

    @Transactional
    public ProfileDto updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request) {
        String url = uploadImageViaGrpc(request.getBase64ImageString());
        Profile profile = getOrCreateProfile(userPrincipal);
        profile.setProfileImageUrl(url);
        profileRepository.save(profile);
        return toDto(profile);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Resolves or auto-creates a {@link Profile} from the session principal.
     * Mirrors original monolith logic: first OAuth2 login creates the profile row.
     */
    @Transactional
    public Profile getOrCreateProfile(UserPrincipal userPrincipal) {
        Profile profile = profileRepository.findOneByUserId(userPrincipal.getId().toString());
        if (Objects.isNull(profile)) {
            profile = new Profile();
            profile.setUserId(userPrincipal.getId().toString());
            profile.setDisplayName(userPrincipal.getName());
            profileRepository.save(profile);
            log.info("Auto-provisioned profile for userId={}", userPrincipal.getId());
        }
        return profile;
    }

    public Profile getProfileById(int id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + id));
    }

    /** Delegates base64 image upload to media-service via gRPC. */
    private String uploadImageViaGrpc(String base64) {
        try {
            UploadImageResponse response = mediaServiceStub.uploadImage(
                    UploadImageRequest.newBuilder().setBase64Image(base64).build()
            );
            if (response.getSuccess()) return response.getUrl();
            log.error("Media service upload failed: {}", response.getErrorMessage());
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to media-service failed: {}", e.getStatus());
        }
        return null;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    public ProfileDto toDto(Profile p) {
        return ProfileDto.builder()
                .id(p.getId())
                .displayName(p.getDisplayName())
                .username(p.getUsername())
                .bio(p.getBio())
                .profileImageUrl(p.getProfileImageUrl())
                .build();
    }
}
