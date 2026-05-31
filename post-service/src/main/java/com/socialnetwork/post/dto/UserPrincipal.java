package com.socialnetwork.post.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents the authenticated principal in the post-service.
 * This is deserialized from the shared Redis session written by the monolith/user-service.
 * OAuth2User is NOT implemented here — only UserDetails for session-based auth.
 */
@Data
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private String name;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private String provider;
    private String providerId;
    private boolean enabled = true;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal() {}

    public UserPrincipal(UUID id, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(UUID id, String username) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(id, username, null, authorities);
    }
}
