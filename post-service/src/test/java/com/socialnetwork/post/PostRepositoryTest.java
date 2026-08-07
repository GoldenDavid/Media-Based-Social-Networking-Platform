package com.socialnetwork.post;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.repository.PostRepository;

@DataJpaTest
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void testSaveAndFindByCreatedByProfileId() {
        Post post = Post.builder()
                .createdByProfileId(42)
                .imageUrl("http://example.com/image.jpg")
                .caption("Hello World")
                .createdAt(new Date())
                .build();

        Post saved = postRepository.save(post);
        assertThat(saved.getId()).isGreaterThan(0);

        List<Post> results = postRepository.findByCreatedByProfileId(42);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCaption()).isEqualTo("Hello World");
    }
}
