package com.socialnetwork.notification.grpc;

import org.springframework.stereotype.Service;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.grpc.profile.GetProfileByUserIdRequest;
import com.socialnetwork.grpc.profile.GetProfileRequest;
import com.socialnetwork.grpc.profile.ProfileResponse;
import com.socialnetwork.grpc.profile.ProfileServiceGrpc;
import com.socialnetwork.notification.dto.ProfileDto;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Service
public class ProfileServiceGrpcClient {

    @GrpcClient("profile-service")
    private ProfileServiceGrpc.ProfileServiceBlockingStub stub;

    public ProfileDto getProfile(int profileId) {
        try {
            ProfileResponse r = stub.getProfile(
                GetProfileRequest.newBuilder().setProfileId(profileId).build());
            return toDto(r);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetProfile failed for profileId={}: {}", profileId, e.getStatus());
            return null; // Return null if profile not found
        }
    }

    /**
     * Resolves a profile from a session principal by calling
     * {@code getOrCreateProfileByUserId}. Used by the
     * notification-service to translate the session's userId (UUID) into
     * the numeric {@code profileId} that the notification table is keyed on.
     */
    public ProfileDto getProfile(UserPrincipal principal) {
        try {
            ProfileResponse r = stub.getOrCreateProfileByUserId(
                GetProfileByUserIdRequest.newBuilder()
                    .setUserId(principal.getId().toString())
                    .setDisplayName(principal.getName() != null ? principal.getName() : "")
                    .build()
            );
            return toDto(r);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetOrCreateProfileByUserId failed for userId={}: {}",
                    principal.getId(), e.getStatus());
            return null;
        }
    }

    private ProfileDto toDto(ProfileResponse r) {
        return ProfileDto.builder()
            .id(r.getId())
            .displayName(r.getDisplayName())
            .username(r.getUsername())
            .profileImageUrl(r.getProfileImageUrl())
            .build();
    }
}
