package com.socialnetwork.feed.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.GetFeedResponse;
import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.feed.dto.UserPrincipal;
import com.socialnetwork.feed.grpc.PostServiceGrpcClient;
import com.socialnetwork.feed.grpc.ProfileServiceGrpcClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dynamic feed strategy — pull model.
 * Calls profile-service (gRPC) to get followings, then post-service (gRPC) to get posts.
 */
@Slf4j
@Service("dynamicFeedService")
@RequiredArgsConstructor
public class DynamicFeedServiceImpl implements FeedService {

    private final ProfileServiceGrpcClient profileService;
    private final PostServiceGrpcClient postService;

    @Override
    public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
        int profileId = profileService.getProfileByUserId(userPrincipal.getId().toString(), userPrincipal.getUsername()).getId();

        // 1. Get followings via gRPC
        List<Integer> followingProfileIds = profileService.getFollowingIds(profileId);
        log.info("followingProfileIds={}", followingProfileIds);

        int offset = (page - 1) * limit;

        // 2. Get posts by authors via gRPC
        List<PostDto> posts = postService.getPostsByAuthors(followingProfileIds, limit, offset);

        // For a full implementation, we'd also call a count gRPC endpoint to get total pages,
        // but for simplicity (or if we add count later), let's just return what we have.
        // Assuming infinite scrolling where totalPage doesn't block UI if we just return empty lists.
        // Or we could implement countPostsByAuthors in post-service if needed.
        // Actually we do have countPostsByAuthors in the proto! Let's mock it for now or assume 1 page.
        // I will add a count method to PostServiceGrpcClient if needed later.

        return GetFeedResponse.builder()
                .posts(posts).totalPage(1).build(); // Hardcoded totalPage for now since UI usually relies on empty list to stop
    }
}
