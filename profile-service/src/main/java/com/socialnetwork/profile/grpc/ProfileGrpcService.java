package com.socialnetwork.profile.grpc;

import com.socialnetwork.grpc.profile.*;
import com.socialnetwork.profile.model.Profile;
import com.socialnetwork.profile.model.UserFollowing;
import com.socialnetwork.profile.repository.FollowerRepository;
import com.socialnetwork.profile.repository.ProfileRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * gRPC server implementation for {@link ProfileServiceGrpc}.
 *
 * <p>Replaces all direct inter-service calls that previously used injected
 * {@code ProfileService} beans or {@code FollowerRepository} JPA queries.
 *
 * <p>Consumers:
 * <ul>
 *   <li><b>Post Service</b> — calls {@link #getOrCreateProfileByUserId} to resolve the author</li>
 *   <li><b>Feed Service (dynamic)</b> — calls {@link #getFollowings} to get followed profiles</li>
 *   <li><b>Feed Service (push)</b> — calls {@link #getFollowers} to fan-out a new post</li>
 *   <li><b>Notification Service</b> — calls {@link #getProfile} to resolve from/to users</li>
 * </ul>
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ProfileGrpcService extends ProfileServiceGrpc.ProfileServiceImplBase {

    private final ProfileRepository profileRepository;
    private final FollowerRepository followerRepository;

    // ── GetProfile ────────────────────────────────────────────────────────────

    @Override
    public void getProfile(GetProfileRequest request,
                           StreamObserver<ProfileResponse> responseObserver) {
        log.debug("gRPC GetProfile profileId={}", request.getProfileId());

        profileRepository.findById(request.getProfileId())
                .map(this::toResponse)
                .ifPresentOrElse(
                        response -> {
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                Status.NOT_FOUND
                                        .withDescription("Profile not found: " + request.getProfileId())
                                        .asRuntimeException()
                        )
                );
    }

    // ── GetOrCreateProfileByUserId ────────────────────────────────────────────

    /**
     * Resolves (or lazily creates) a {@link Profile} from an OAuth2 user UUID.
     * This mirrors the monolith's {@code ProfileServiceImpl#getProfileEntity(UserPrincipal)} logic.
     */
    @Override
    @Transactional
    public void getOrCreateProfileByUserId(GetProfileByUserIdRequest request,
                                           StreamObserver<ProfileResponse> responseObserver) {
        log.debug("gRPC GetOrCreateProfileByUserId userId={}", request.getUserId());

        Profile profile = profileRepository.findOneByUserId(request.getUserId());

        if (Objects.isNull(profile)) {
            profile = new Profile();
            profile.setUserId(request.getUserId());
            profile.setDisplayName(request.getDisplayName());
            profileRepository.save(profile);
            log.info("Auto-provisioned new profile for userId={}", request.getUserId());
        }

        responseObserver.onNext(toResponse(profile));
        responseObserver.onCompleted();
    }

    // ── GetFollowings ─────────────────────────────────────────────────────────

    @Override
    public void getFollowings(GetFollowingsRequest request,
                              StreamObserver<FollowingsResponse> responseObserver) {
        log.debug("gRPC GetFollowings profileId={}", request.getProfileId());

        List<Integer> ids = followerRepository
                .findByFollowerUserId(request.getProfileId())
                .stream()
                .map(UserFollowing::getFollowingUserId)
                .toList();

        responseObserver.onNext(
                FollowingsResponse.newBuilder()
                        .addAllFollowingProfileIds(ids)
                        .setTotal(ids.size())
                        .build()
        );
        responseObserver.onCompleted();
    }

    // ── GetFollowers ──────────────────────────────────────────────────────────

    @Override
    public void getFollowers(GetFollowersRequest request,
                             StreamObserver<FollowersResponse> responseObserver) {
        log.debug("gRPC GetFollowers profileId={}", request.getProfileId());

        List<Integer> ids = followerRepository
                .findByFollowingUserId(request.getProfileId())
                .stream()
                .map(UserFollowing::getFollowerUserId)
                .toList();

        responseObserver.onNext(
                FollowersResponse.newBuilder()
                        .addAllFollowerProfileIds(ids)
                        .setTotal(ids.size())
                        .build()
        );
        responseObserver.onCompleted();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ProfileResponse toResponse(Profile p) {
        return ProfileResponse.newBuilder()
                .setId(p.getId())
                .setDisplayName(p.getDisplayName() != null ? p.getDisplayName() : "")
                .setUsername(p.getUsername() != null ? p.getUsername() : "")
                .setBio(p.getBio() != null ? p.getBio() : "")
                .setProfileImageUrl(p.getProfileImageUrl() != null ? p.getProfileImageUrl() : "")
                .setUserId(p.getUserId() != null ? p.getUserId() : "")
                .build();
    }
}
