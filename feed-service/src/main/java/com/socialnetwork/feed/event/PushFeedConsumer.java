package com.socialnetwork.feed.event;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.socialnetwork.feed.config.MessageQueueConfig;
import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.feed.grpc.PostServiceGrpcClient;
import com.socialnetwork.feed.grpc.ProfileServiceGrpcClient;
import com.socialnetwork.feed.repository.FeedRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ consumer for the {@code after-create-post-queue}.
 * Fans out a newly created post to each follower's Redis feed list.
 */
@Slf4j
@Component
@RabbitListener(queues = MessageQueueConfig.AFTER_CREATE_POST_QUEUE)
public class PushFeedConsumer {

    @Autowired
    private PostServiceGrpcClient postService;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ProfileServiceGrpcClient profileService;

    @RabbitHandler
    public void receive(Integer postId) {
        log.info("[x] Received postId={}", postId);

        // Fetch post using gRPC to get the author's profile ID
        List<PostDto> posts = postService.getPostsByIds(List.of(postId));
        if (posts.isEmpty()) {
            log.error("Post {} not found when fanning out", postId);
            return;
        }

        PostDto post = posts.get(0);
        int authorProfileId = post.getCreatedBy().getId();

        // Fetch followers using gRPC
        List<Integer> followerIds = profileService.getFollowerIds(authorProfileId);
        log.info("Fanning out postId={} to {} followers", postId, followerIds.size());

        // Add post to author's own feed
        feedRepository.addPostToFeed(postId, authorProfileId);

        for (int followerProfileId : followerIds) {
            feedRepository.addPostToFeed(postId, followerProfileId);
        }
    }
}
