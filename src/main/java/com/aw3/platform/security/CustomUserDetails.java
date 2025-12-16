package com.aw3.platform.security;

import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom UserDetails implementation for Spring Security
 */
@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private UUID userId;
    private String walletAddress;
    private String didIdentifier;
    private UserRole userRole;
    private boolean enabled;

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getUserId(),
                user.getWalletAddress(),
                user.getDidIdentifier(),
                user.getUserRole(),
                user.getStatus() == com.aw3.platform.entity.enums.UserStatus.ACTIVE
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    public String getPassword() {
        return null; // No password for wallet-based auth
    }

    @Override
    public String getUsername() {
        return walletAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

