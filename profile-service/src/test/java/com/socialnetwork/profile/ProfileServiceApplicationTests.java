package com.socialnetwork.profile;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that the profile-service {@link ApplicationContext} can be
 * created in the {@code test} profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProfileServiceApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
        assertThat(context.getBean(ProfileServiceApplication.class)).isNotNull();
    }
}
