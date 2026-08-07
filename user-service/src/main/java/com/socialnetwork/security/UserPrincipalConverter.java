package com.socialnetwork.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.model.User;

public final class UserPrincipalConverter {

    private UserPrincipalConverter() {
    }

    public static UserPrincipal fromUser(User user) {
        return fromUser(user, null);
    }

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
