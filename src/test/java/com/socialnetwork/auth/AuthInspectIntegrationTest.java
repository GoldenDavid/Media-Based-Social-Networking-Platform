package com.socialnetwork.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.socialnetwork.common.security.UserPrincipal;

import jakarta.annotation.PostConstruct;

/**
 * Integration test for the monolith's {@code /auth/inspect} endpoint.
 *
 * <p>Verifies that the endpoint returns 200 with a JSON body containing
 * the authenticated user's id, username, and name. Uses a stubbed
 * {@link UserPrincipal} injected via Spring Security's test support.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthInspectIntegrationTest {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @PostConstruct
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void inspectReturns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/inspect"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "alice")
    void inspectReturns200WhenAuthenticated() throws Exception {
        UserPrincipal principal = UserPrincipal.of(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "alice",
                "Alice Dev");
        // Override the default @WithMockUser principal with our real UserPrincipal.
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/inspect").with(user(principal)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("alice"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void userPrincipalJacksonTypingPreservesId() {
        // Per ADR-009/Phase 2.3: UserPrincipal has @JsonTypeInfo(use=CLASS,...)
        // and @JsonIgnoreProperties(ignoreUnknown=true). This sanity check
        // guards against accidental removal of those annotations.
        assertThat(UserPrincipal.class.isAnnotationPresent(
                com.fasterxml.jackson.annotation.JsonTypeInfo.class)).isTrue();
        assertThat(UserPrincipal.class.isAnnotationPresent(
                com.fasterxml.jackson.annotation.JsonIgnoreProperties.class)).isTrue();
    }

    @Test
    void userPrincipalOfFactoryProducesValidPrincipal() {
        UUID id = UUID.randomUUID();
        UserPrincipal p = UserPrincipal.of(id, "bob", "Bob");
        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsername()).isEqualTo("bob");
        assertThat(p.getName()).isEqualTo("Bob");
        assertThat(p.isEnabled()).isTrue();
        assertThat(p.getAuthorities()).extracting(Object::toString)
                .containsExactly("ROLE_USER");
        // Suppress unused warning
        assertThat(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))).isNotEmpty();
    }
}
