package com.socialnetwork.event;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.socialnetwork.config.MessageQueueConfig;
import com.socialnetwork.model.Post;
import com.socialnetwork.repository.FeedRepository;
import com.socialnetwork.service.feed.PostService;
import com.socialnetwork.service.profile.ProfileServiceGrpcClient;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ consumer for the {@code after-create-post-queue}.
 *
 * <p>Fans out a newly created post to each follower's Redis feed list.
 *
 * <p><b>Before (monolith)</b>: called {@code FollowerRepository.findByFollowingUserId()} directly.
 * <p><b>After (microservices)</b>: calls {@link ProfileServiceGrpcClient#getFollowerIds}
 * via gRPC — the monolith no longer reads {@code user_following} directly.
 */
@Slf4j
@RabbitListener(queues = MessageQueueConfig.AFTER_CREATE_POST_QUEUE)
public class PushFeedConsumer {

    @Autowired
    PostService postService;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    ProfileServiceGrpcClient profileServiceGrpcClient;

    @RabbitHandler
    public void receive(Integer postId) {
        log.info("[x] Received postId={}", postId);

        Post post = postService.getPostEntity(postId);
        int authorProfileId = post.getCreatedBy().getId();

        // ── gRPC call replaces: followerRepository.findByFollowingUserId(authorProfileId) ──
        List<Integer> followerIds = profileServiceGrpcClient.getFollowerIds(authorProfileId);
        log.info("Fanning out postId={} to {} followers", postId, followerIds.size());

        for (int followerProfileId : followerIds) {
            feedRepository.addPostToFeed(post.getId(), followerProfileId);
        }
    }
}

