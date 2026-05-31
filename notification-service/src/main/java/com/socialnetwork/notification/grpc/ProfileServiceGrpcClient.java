package com.socialnetwork.notification.grpc;

import org.springframework.stereotype.Service;
import com.socialnetwork.notification.dto.ProfileDto;
import com.socialnetwork.grpc.profile.GetProfileRequest;
import com.socialnetwork.grpc.profile.ProfileResponse;
import com.socialnetwork.grpc.profile.ProfileServiceGrpc;
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
            return ProfileDto.builder()
                .id(r.getId())
                .displayName(r.getDisplayName())
                .username(r.getUsername())
                .profileImageUrl(r.getProfileImageUrl())
                .build();
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetProfile failed for profileId={}: {}", profileId, e.getStatus());
            return null; // Return null if profile not found
        }
    }
}
