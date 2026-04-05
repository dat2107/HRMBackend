package com.toto.backend.service;

import com.toto.backend.entity.Account;
import com.toto.backend.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final String employeeId;
    private final String passwordHash;
    private final Role role;
    private final Integer tokenVersion;
    private final boolean accountLocked;

    public CustomUserDetails(Account account) {
        this.employeeId = account.getEmployeeId();
        this.passwordHash = account.getPasswordHash();
        this.role = account.getRole();
        this.tokenVersion = account.getTokenVersion() != null ? account.getTokenVersion() : 0;
        this.accountLocked = Boolean.TRUE.equals(account.getIsLocked());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return employeeId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
