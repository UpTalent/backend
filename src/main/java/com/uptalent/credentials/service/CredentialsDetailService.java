package com.uptalent.credentials.service;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.principal.AccountPrincipal;
import com.uptalent.credentials.repository.CredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialsDetailService implements UserDetailsService {
    private final CredentialsRepository credentialsRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credentials not found by email [ " + email + "]"));

        return new AccountPrincipal(credentials);
    }
}
