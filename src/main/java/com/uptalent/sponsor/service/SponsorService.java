package com.uptalent.sponsor.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.payload.AuthResponse;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${sponsor.initial-kudos-number}")
    private int INITIAL_KUDOS_NUMBER;

    public AuthResponse registerSponsor(SponsorRegistration sponsorRegistration) {
        if (credentialsRepository.existsByEmailIgnoreCase(sponsorRegistration.getEmail())){
            throw new AccountExistsException("The user has already exists with email [" + sponsorRegistration.getEmail() + "]");
        }

        var credentials = Credentials.builder()
                .email(sponsorRegistration.getEmail())
                .password(passwordEncoder.encode(sponsorRegistration.getPassword()))
                .status(AccountStatus.ACTIVE)
                .role(Role.SPONSOR)
                .build();

        credentialsRepository.save(credentials);

        var savedSponsor = sponsorRepository.save(Sponsor.builder()
                .credentials(credentials)
                .fullname(sponsorRegistration.getFullname())
                .kudos(INITIAL_KUDOS_NUMBER)
                .build());

        String jwtToken = jwtTokenProvider.generateJwtToken(
                savedSponsor.getCredentials().getEmail(),
                savedSponsor.getId(),
                savedSponsor.getCredentials().getRole(),
                savedSponsor.getFullname()
        );
        return new AuthResponse(jwtToken);
    }
}
