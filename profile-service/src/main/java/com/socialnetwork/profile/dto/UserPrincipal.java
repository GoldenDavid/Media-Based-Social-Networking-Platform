package com.socialnetwork.profile.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Wraps the authenticated session user.
 * Mirrors the monolith's UserPrincipal — used by REST controllers to extract
 * the current user's identity from the Spring Security context.
 */
public class UserPrincipal implements OAuth2User, UserDetails {

    private final UUID id;
    private final String name;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public UserPrincipal(UUID id, String name, String username,
                         Collection<? extends GrantedAuthority> authorities,
                         Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    public UUID getId() { return id; }

    @Override public String getName()     { return name; }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return null; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public Map<String, Object> getAttributes() { return attributes; }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
