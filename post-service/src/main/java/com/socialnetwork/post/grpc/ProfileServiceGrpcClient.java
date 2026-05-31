package com.socialnetwork.post.grpc;

import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.grpc.profile.*;
import com.socialnetwork.post.model.Profile;
import com.socialnetwork.post.service.ProfileService;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * gRPC-based implementation of {@link ProfileService}.
 *
 * <p>Replaces the direct JPA {@link ProfileServiceImpl} by delegating all
 * profile queries to the standalone <b>profile-service</b> via gRPC.
 *
 * <p>Annotated {@code @Primary} so Spring injects this bean wherever
 * {@link ProfileService} is requested, without any changes to callers
 * (PostServiceImpl, CommentServiceImpl, DynamicFeedServiceImpl etc.).
 *
 * <p>The {@code @GrpcClient("profile-service")} annotation wires the managed
 * gRPC channel configured under {@code grpc.client.profile-service.*} in
 * application.yml.
 */
@Slf4j
@Primary
@Service("profileServiceGrpcClient")
public class ProfileServiceGrpcClient implements ProfileService {

    @GrpcClient("profile-service")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileServiceStub;

    // ── ProfileService interface ───────────────────────────────────────────────

    @Override
    public ProfileDto getUserProfile(UserPrincipal userPrincipal) {
        return toDto(getProfileEntity(userPrincipal));
    }

    @Override
    public ProfileDto getUserProfile(int id) {
        return toDto(getProfileEntity(id));
    }

    /**
     * Resolves (or auto-creates) the current user's profile via gRPC.
     * Called by PostServiceImpl, CommentServiceImpl, etc.
     */
    @Override
    public Profile getProfileEntity(UserPrincipal userPrincipal) {
        log.debug("gRPC GetOrCreateProfileByUserId userId={}", userPrincipal.getId());
        try {
            ProfileResponse response = profileServiceStub.getOrCreateProfileByUserId(
                    GetProfileByUserIdRequest.newBuilder()
                            .setUserId(userPrincipal.getId().toString())
                            .setDisplayName(userPrincipal.getName() != null ? userPrincipal.getName() : "")
                            .build()
            );
            return fromResponse(response);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetOrCreateProfileByUserId failed: {}", e.getStatus());
            throw new RuntimeException("Profile service unavailable", e);
        }
    }

    @Override
    public Profile getProfileEntity(int profileId) {
        log.debug("gRPC GetProfile profileId={}", profileId);
        try {
            ProfileResponse response = profileServiceStub.getProfile(
                    GetProfileRequest.newBuilder().setProfileId(profileId).build()
            );
            return fromResponse(response);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetProfile failed for profileId={}: {}", profileId, e.getStatus());
            throw new RuntimeException("Profile not found: " + profileId, e);
        }
    }

    /**
     * Returns the profile IDs that {@code profileId} is following.
     * Called by {@link com.socialnetwork.service.feed.DynamicFeedServiceImpl}.
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
     * Called by {@link com.socialnetwork.event.PushFeedConsumer} during fan-out.
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

    // ── Mappers ───────────────────────────────────────────────────────────────

    private Profile fromResponse(ProfileResponse r) {
        Profile p = new Profile();
        p.setId(r.getId());
        p.setDisplayName(r.getDisplayName());
        p.setUsername(r.getUsername());
        p.setBio(r.getBio());
        p.setProfileImageUrl(r.getProfileImageUrl());
        p.setUserId(r.getUserId());
        return p;
    }

    private ProfileDto toDto(Profile p) {
        return ProfileDto.builder()
                .id(p.getId())
                .displayName(p.getDisplayName())
                .username(p.getUsername())
                .bio(p.getBio())
                .profileImageUrl(p.getProfileImageUrl())
                .build();
    }
}
