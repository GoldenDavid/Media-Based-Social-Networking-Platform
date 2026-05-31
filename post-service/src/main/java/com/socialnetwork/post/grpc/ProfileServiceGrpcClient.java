package com.socialnetwork.post.grpc;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.socialnetwork.grpc.profile.FollowersResponse;
import com.socialnetwork.grpc.profile.FollowingsResponse;
import com.socialnetwork.grpc.profile.GetFollowersRequest;
import com.socialnetwork.grpc.profile.GetFollowingsRequest;
import com.socialnetwork.grpc.profile.GetProfileByUserIdRequest;
import com.socialnetwork.grpc.profile.GetProfileRequest;
import com.socialnetwork.grpc.profile.ProfileResponse;
import com.socialnetwork.grpc.profile.ProfileServiceGrpc;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.post.service.ProfileService;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * gRPC-based implementation of {@link ProfileService}.
 *
 * <p>Delegates all profile queries to the standalone <b>profile-service</b> via gRPC.
 * Returns {@link ProfileDto} directly — no JPA Profile entity is constructed in post-service.
 *
 * <p>Annotated {@code @Primary} so Spring injects this bean wherever
 * {@link ProfileService} is requested.
 */
@Slf4j
@Primary
@Service("profileServiceGrpcClient")
public class ProfileServiceGrpcClient implements ProfileService {

    @GrpcClient("profile-service")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileServiceStub;

    // ── ProfileService interface ───────────────────────────────────────────────

    @Override
    public ProfileDto getProfile(UserPrincipal userPrincipal) {
        log.debug("gRPC GetOrCreateProfileByUserId userId={}", userPrincipal.getId());
        try {
            ProfileResponse response = profileServiceStub.getOrCreateProfileByUserId(
                    GetProfileByUserIdRequest.newBuilder()
                            .setUserId(userPrincipal.getId().toString())
                            .setDisplayName(userPrincipal.getName() != null ? userPrincipal.getName() : "")
                            .build()
            );
            return toDto(response);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetOrCreateProfileByUserId failed: {}", e.getStatus());
            throw new RuntimeException("Profile service unavailable", e);
        }
    }

    @Override
    public ProfileDto getProfile(int profileId) {
        log.debug("gRPC GetProfile profileId={}", profileId);
        try {
            ProfileResponse response = profileServiceStub.getProfile(
                    GetProfileRequest.newBuilder().setProfileId(profileId).build()
            );
            return toDto(response);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetProfile failed for profileId={}: {}", profileId, e.getStatus());
            throw new RuntimeException("Profile not found: " + profileId, e);
        }
    }

    @Override
    public ProfileDto getUserProfile(int profileId) {
        return getProfile(profileId);
    }

    // ── Social graph helpers ──────────────────────────────────────────────────

    /**
     * Returns the profile IDs that {@code profileId} is following.
     */
    public List<Integer> getFollowingIds(int profileId) {
        log.debug("gRPC GetFollowings profileId={}", profileId);
        try {
            FollowingsResponse response = profileServiceStub.getFollowings(
                    GetFollowingsRequest.newBuilder().setProfileId(profileId).build()
            );
            return response.getFollowingProfileIdsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetFollowings failed for profileId={}: {}", profileId, e.getStatus());
            return List.of();
        }
    }

    /**
     * Returns the profile IDs of followers of {@code profileId}.
     */
    public List<Integer> getFollowerIds(int profileId) {
        log.debug("gRPC GetFollowers profileId={}", profileId);
        try {
            FollowersResponse response = profileServiceStub.getFollowers(
                    GetFollowersRequest.newBuilder().setProfileId(profileId).build()
            );
            return response.getFollowerProfileIdsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetFollowers failed for profileId={}: {}", profileId, e.getStatus());
            return List.of();
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private ProfileDto toDto(ProfileResponse r) {
        return ProfileDto.builder()
                .id(r.getId())
                .displayName(r.getDisplayName())
                .username(r.getUsername())
                .bio(r.getBio())
                .profileImageUrl(r.getProfileImageUrl())
                .userId(r.getUserId())
                .build();
    }
}
