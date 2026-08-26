package com.socialnetwork.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that the post-service {@link ApplicationContext} can be
 * created in the {@code test} profile. Verifies Spring Boot autoconfig,
 * JPA wiring, gRPC server bootstrap, security, and the session/Redis
 * autoconfig exclusions all play nicely together.
 *
 * <p>The {@link RabbitTemplate} is replaced with a no-op {@code @Primary}
 * bean (rather than a Mockito mock) because Mockito's inline mock maker
 * is incompatible with newer JDKs without extra {@code --add-opens} flags.
 * The gRPC server is disabled in test config (port = -1) since the
 * in-process clients are configured with static addresses that never
 * connect.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostServiceApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
        assertThat(context.getBean(PostServiceApplication.class)).isNotNull();
    }

    @org.springframework.boot.test.mock.mockito.MockBean
    com.socialnetwork.post.repository.PostSearchRepository postSearchRepository;

    /**
     * Provide a stub {@link RabbitTemplate} so the service constructor
     * can be satisfied without an actual broker. The bean is intentionally
     * a no-op — no messages are sent during context-load.
     */
    @TestConfiguration
    static class RabbitStubConfig {
        @Bean
        @Primary
        RabbitTemplate rabbitTemplate() {
            // CachingConnectionFactory lazily connects; if we never
            // actually call send/receive, the broker is never touched.
            org.springframework.amqp.rabbit.connection.CachingConnectionFactory cf =
                    new org.springframework.amqp.rabbit.connection.CachingConnectionFactory();
            cf.setHost("localhost");
            cf.setPort(5672);
            return new RabbitTemplate(cf);
        }
    }
}
