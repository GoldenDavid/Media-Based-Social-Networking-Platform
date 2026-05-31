package com.socialnetwork.service.feed;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.GetFeedResponse;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.profile.ProfileService;
import com.socialnetwork.service.profile.ProfileServiceGrpcClient;
import com.socialnetwork.util.MapperUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dynamic feed strategy — pull model.
 *
 * <p><b>Before (monolith)</b>: queried {@code FollowerRepository} directly (JPA).
 * <p><b>After (microservices)</b>: calls {@link ProfileServiceGrpcClient#getFollowingIds}
 * via gRPC, enforcing the profile-service boundary. The monolith no longer reads the
 * {@code user_following} table directly.
 */
@Slf4j
@Service("dynamicFeedService")
@RequiredArgsConstructor
public class DynamicFeedServiceImpl implements FeedService {

    private final ProfileService profileService;
    private final PostRepository postRepository;
    private final ProfileServiceGrpcClient profileServiceGrpcClient;

    @Override
    public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
        Profile profile = profileService.getProfileEntity(userPrincipal);

        // ── gRPC call replaces: followerRepository.findByFollowerUserId(profile.getId()) ──
        List<Integer> followingProfileIds = profileServiceGrpcClient.getFollowingIds(profile.getId());
        log.info("followingProfileIds={}", followingProfileIds);

        int totalPost = postRepository.countByCreatedByIn(followingProfileIds);
        log.info("totalPost={}", totalPost);
        int totalPage = (int) Math.ceil((double) totalPost / limit);
        int offset = (page - 1) * limit;

        List<PostDto> posts = postRepository
                .findByCreatedBy(followingProfileIds, limit, offset)
                .stream().map(MapperUtils::toDto).toList();

        return GetFeedResponse.builder()
                .posts(posts).totalPage(totalPage).build();
    }
}

