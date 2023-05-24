package com.uptalent.email.service;

import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmailService {
    private final CredentialsRepository credentialsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private void changeStatusToActive(Credentials credentials) {
        credentials.setStatus(AccountStatus.ACTIVE);
        credentials.setExpirationDeleting(null);
        credentials.setDeleteToken(null);
        credentialsRepository.save(credentials);
    }

    public void restoreAccount(String token) {
        Credentials credentials = credentialsRepository.findCredentialsByDeleteToken(LocalDateTime.now(), token)
                .orElseThrow(() -> new SponsorNotFoundException("Token is invalid"));
        changeStatusToActive(credentials);
    }

    public AuthResponse verifyAccount(String token) {
        Credentials credentials = credentialsRepository.findCredentialsByDeleteToken(LocalDateTime.now(), token)
                .orElseThrow(() -> new SponsorNotFoundException("Token is invalid"));
        credentials.setVerified(true);
        changeStatusToActive(credentials);
        String jwtToken = getJwtByRole(credentials);
        return new AuthResponse(jwtToken);
    }
    public String getJwtByRole(Credentials credentials){
        Long id;
        String name;
        if(credentials.getRole().equals(Role.TALENT)) {
            id = credentials.getTalent().getId();
            name = credentials.getTalent().getFirstname();
        }
        else {
            id = credentials.getSponsor().getId();
            name = credentials.getSponsor().getFullname();
        }
        return jwtTokenProvider.generateJwtToken(
                credentials.getEmail(),
                id,
                credentials.getRole(),
                name
        );
    }
}
