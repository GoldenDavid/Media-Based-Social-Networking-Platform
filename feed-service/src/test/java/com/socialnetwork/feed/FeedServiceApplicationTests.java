package com.socialnetwork.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that the feed-service {@link ApplicationContext} can be
 * created in the {@code test} profile. The gRPC clients (profile-service,
 * post-service) are configured with static addresses that are never
 * connected to during context load, so this test runs offline.
 */
@SpringBootTest
@ActiveProfiles("test")
class FeedServiceApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
        assertThat(context.getBean(FeedServiceApplication.class)).isNotNull();
    }
}
