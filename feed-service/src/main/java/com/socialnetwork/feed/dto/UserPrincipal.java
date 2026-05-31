package com.socialnetwork.feed.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private String name;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal() {}

    public UserPrincipal(UUID id, String username) {
        this.id = id;
        this.username = username;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
