package com.socialnetwork.feed.repository;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis-backed feed store.
 * Stores ordered post ID lists per profile under key {@code feed:{profileId}}.
 */
@Repository
@RequiredArgsConstructor
public class FeedRepository {

    private static final String FEED_KEY_PREFIX = "feed:";

    private final RedisTemplate<String, Long> redisTemplate;

    public Long getFeedSize(int profileId) {
        return redisTemplate.opsForList().size(FEED_KEY_PREFIX + profileId);
    }

    public void addPostToFeed(int postId, int profileId) {
        redisTemplate.opsForList().leftPush(FEED_KEY_PREFIX + profileId, Long.valueOf(postId));
    }

    public List<Long> getFeed(int profileId, int limit, int page) {
        int start = (page - 1) * limit;
        int end = start + limit - 1;
        return redisTemplate.opsForList().range(FEED_KEY_PREFIX + profileId, start, end);
    }
}
