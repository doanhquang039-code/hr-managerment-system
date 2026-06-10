package com.example.hr.service;

import com.example.hr.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails that resolves Spring Security authority from GroupRole (DB-driven)
 * with fallback to legacy Role enum via getEffectiveRoleName().
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // getEffectiveRoleName() returns groupRole.name if set, otherwise legacy Role enum name
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getEffectiveRoleName())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null &&
               user.getStatus() == com.example.hr.enums.UserStatus.ACTIVE;
    }
}
