package com.socialnetwork.feed.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.GetFeedResponse;
import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.common.security.UserPrincipal;
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

        // Add self to see own posts
        List<Integer> feedProfileIds = new java.util.ArrayList<>(followingProfileIds);
        feedProfileIds.add(profileId);

        int offset = (page - 1) * limit;

        List<PostDto> posts = postService.getPostsByAuthors(feedProfileIds, limit, offset);

        int totalPosts = postService.countPostsByAuthors(feedProfileIds);
        int totalPage = totalPosts == 0 ? 0 : (int) Math.ceil((double) totalPosts / limit);

        return GetFeedResponse.builder()
                .posts(posts).totalPage(totalPage).build();
    }
}
