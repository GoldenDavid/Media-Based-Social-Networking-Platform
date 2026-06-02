package com.socialnetwork.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.model.User;

/**
 * Converts the monolith's {@link User} model into a {@link UserPrincipal}
 * for use as the {@code Authentication} principal.
 *
 * <p>This is the only place in the monolith that knows how to map a {@code User}
 * to a {@code UserPrincipal}. Other services don't have a {@code User} model
 * — they receive the principal from the session.
 */
public final class UserPrincipalConverter {

    private UserPrincipalConverter() {
    }

    /**
     * Build a {@link UserPrincipal} from a {@link User}, populating sensible
     * defaults if the user's fields are missing.
     */
    public static UserPrincipal fromUser(User user) {
        return fromUser(user, null);
    }

    /**
     * Build a {@link UserPrincipal} from a {@link User} with OAuth2 attributes
     * attached (e.g. the original {@code id_token} claims).
     */
    public static UserPrincipal fromUser(User user, Map<String, Object> attributes) {
        UserPrincipal principal = new UserPrincipal();
        principal.setId(user.getId());
        principal.setUsername(user.getUsername());
        principal.setPassword(user.getPassword());
        principal.setName(user.getName());
        principal.setAccountNonExpired(user.isAccountNonExpired());
        principal.setAccountNonLocked(user.isAccountNonLocked());
        principal.setCredentialsNonExpired(user.isCredentialsNonExpired());
        principal.setEnabled(user.isEnabled());
        principal.setProvider(user.getProvider());
        principal.setProviderId(user.getProviderId());
        principal.setAuthorities(resolveAuthorities(user));
        principal.setAttributes(attributes);
        return principal;
    }

    private static Collection<? extends GrantedAuthority> resolveAuthorities(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }
}
