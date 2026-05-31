package com.socialnetwork.feed.grpc;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.ProfileDto;
import com.socialnetwork.grpc.profile.FollowersResponse;
import com.socialnetwork.grpc.profile.FollowingsResponse;
import com.socialnetwork.grpc.profile.GetFollowersRequest;
import com.socialnetwork.grpc.profile.GetFollowingsRequest;
import com.socialnetwork.grpc.profile.GetProfileByUserIdRequest;
import com.socialnetwork.grpc.profile.GetProfileRequest;
import com.socialnetwork.grpc.profile.ProfileResponse;
import com.socialnetwork.grpc.profile.ProfileServiceGrpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * gRPC client for the profile-service.
 * Used by DynamicFeedServiceImpl (getFollowingIds) and PushFeedConsumer (getFollowerIds).
 */
@Slf4j
@Service
public class ProfileServiceGrpcClient {

    @GrpcClient("profile-service")
    private ProfileServiceGrpc.ProfileServiceBlockingStub stub;

    public ProfileDto getProfileByUserId(String userId, String displayName) {
        try {
            ProfileResponse r = stub.getOrCreateProfileByUserId(
                GetProfileByUserIdRequest.newBuilder()
                    .setUserId(userId)
                    .setDisplayName(displayName != null ? displayName : "")
                    .build());
            return toDto(r);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetOrCreateProfileByUserId failed: {}", e.getStatus());
            throw new RuntimeException("Profile service unavailable", e);
        }
    }

    public ProfileDto getProfile(int profileId) {
        try {
            ProfileResponse r = stub.getProfile(
                GetProfileRequest.newBuilder().setProfileId(profileId).build());
            return toDto(r);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetProfile failed for profileId={}: {}", profileId, e.getStatus());
            throw new RuntimeException("Profile not found: " + profileId, e);
        }
    }

    /**
     * Returns profile IDs that {@code profileId} is following.
     * Used by DynamicFeedServiceImpl to build the pull-based feed.
     */
    public List<Integer> getFollowingIds(int profileId) {
        try {
            FollowingsResponse r = stub.getFollowings(
                GetFollowingsRequest.newBuilder().setProfileId(profileId).build());
            return r.getFollowingProfileIdsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetFollowings failed for profileId={}: {}", profileId, e.getStatus());
            return List.of();
        }
    }

    /**
     * Returns profile IDs of followers of {@code profileId}.
     * Used by PushFeedConsumer for fan-out after post creation.
     */
    public List<Integer> getFollowerIds(int profileId) {
        try {
            FollowersResponse r = stub.getFollowers(
                GetFollowersRequest.newBuilder().setProfileId(profileId).build());
            return r.getFollowerProfileIdsList();
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetFollowers failed for profileId={}: {}", profileId, e.getStatus());
            return List.of();
        }
    }

    private ProfileDto toDto(ProfileResponse r) {
        return ProfileDto.builder()
            .id(r.getId())
            .displayName(r.getDisplayName())
            .username(r.getUsername())
            .bio(r.getBio())
            .profileImageUrl(r.getProfileImageUrl())
            .build();
    }
}
