package com.toto.backend.service.impl;

import com.toto.backend.entity.Account;
import com.toto.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmployeeId(employeeId.toUpperCase())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + employeeId));
        return new CustomUserDetails(account);
    }
}
