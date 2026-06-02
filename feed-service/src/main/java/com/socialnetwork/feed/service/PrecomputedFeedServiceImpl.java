package com.socialnetwork.feed.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.GetFeedResponse;
import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.feed.grpc.PostServiceGrpcClient;
import com.socialnetwork.feed.grpc.ProfileServiceGrpcClient;
import com.socialnetwork.feed.repository.FeedRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("precomputedFeedService")
@RequiredArgsConstructor
public class PrecomputedFeedServiceImpl implements FeedService {

    private final ProfileServiceGrpcClient profileService;
    private final PostServiceGrpcClient postService;
    private final FeedRepository feedRepository;

    @Override
    public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
        int profileId = profileService.getProfileByUserId(userPrincipal.getId().toString(), userPrincipal.getUsername()).getId();

        // 1. Get feed list from Redis
        List<Integer> postIds = feedRepository.getFeed(profileId, limit, page).stream()
                .map(Long::intValue)
                .toList();
        log.info("postIds={}", postIds);

        // 2. Hydrate posts via Post Service gRPC
        List<PostDto> posts = postService.getPostsByIds(postIds);

        Long totalPost = feedRepository.getFeedSize(profileId);
        log.info("totalPost={}", totalPost);
        int totalPage = (int) Math.ceil((double) totalPost / limit);

        return GetFeedResponse.builder()
                .posts(posts).totalPage(totalPage).build();
    }
}
