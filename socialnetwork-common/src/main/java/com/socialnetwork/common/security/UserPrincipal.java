package com.socialnetwork.common.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

/**
 * Canonical session principal shared by all services.
 *
 * <p>This class lives in {@code socialnetwork-common} so that:
 * <ul>
 *   <li>The FQCN is the same on the monolith and every microservice.</li>
 *   <li>Jackson can deserialize the Redis session blob across all services.</li>
 *   <li>The {@code @JsonTypeInfo} discriminator is always present, so polymorphic
 *       deserialization is consistent.</li>
 * </ul>
 *
 * <p><b>Do not duplicate this class.</b> If you need a service-specific extension,
 * add a field here and update the Redis key version if necessary.
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrincipal implements OAuth2User, UserDetails {

    public UserPrincipal() {
    }

    public UserPrincipal(UUID id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

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
    private Map<String, Object> attributes;

    /**
     * Convenience factory for code that has a `User` model and wants a principal
     * with sensible defaults. The `User` model class is intentionally not imported
     * here — services that need this helper can wrap it in their own factory.
     */
    public static UserPrincipal of(UUID id, String username, String name) {
        UserPrincipal p = new UserPrincipal();
        p.setId(id);
        p.setUsername(username);
        p.setName(name);
        p.setEnabled(true);
        p.setAccountNonExpired(true);
        p.setAccountNonLocked(true);
        p.setCredentialsNonExpired(true);
        p.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        return p;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return name != null ? name : username;
    }
}
