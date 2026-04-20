package com.socialnetwork.event;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.socialnetwork.config.MessageQueueConfig;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.UserFollowing;
import com.socialnetwork.repository.FeedRepository;
import com.socialnetwork.repository.FollowerRepository;
import com.socialnetwork.repository.NotificationRepository;
import com.socialnetwork.service.feed.PostService;
import com.socialnetwork.service.profile.FollowerService;
import com.socialnetwork.service.profile.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RabbitListener(queues = MessageQueueConfig.AFTER_CREATE_POST_QUEUE)
public class PushFeedConsumer {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProfileService profileService;

    @Autowired
    PostService postService;

    @Autowired
    FollowerRepository followerRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FeedRepository feedRepository;

    @RabbitHandler
    public void receive(Integer postId) throws JsonMappingException, JsonProcessingException {
        log.info(" [x] Received '" + postId + "'");

        Post post = postService.getPostEntity(postId);

        List<UserFollowing> follwerList = followerRepository.findByFollowingUserId(post.getCreatedBy().getId());

        for (UserFollowing userFollowing : follwerList) {
            log.info("userFollowing={}", userFollowing);
            feedRepository.addPostToFeed(post.getId(), userFollowing.getFollowerUserId());
        }
    }
}
