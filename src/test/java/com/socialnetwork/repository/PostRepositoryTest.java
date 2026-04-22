package com.socialnetwork.repository;

import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    void findByCreatedBy_ShouldReturnPosts() {
        // Arrange
        Profile profile = new Profile();
        profile.setUsername("testuser");
        profile.setEmail("test@example.com");
        profile = entityManager.persist(profile);

        Post post1 = new Post();
        post1.setCaption("Post 1");
        post1.setCreatedBy(profile);
        post1.setCreatedAt(new Date());
        entityManager.persist(post1);

        Post post2 = new Post();
        post2.setCaption("Post 2");
        post2.setCreatedBy(profile);
        post2.setCreatedAt(new Date());
        entityManager.persist(post2);

        entityManager.flush();

        // Act
        List<Post> result = postRepository.findByCreatedBy(profile);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Post::getCaption).containsExactlyInAnyOrder("Post 1", "Post 2");
    }
}
