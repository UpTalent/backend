package com.uptalent.auth.service;

import com.uptalent.auth.model.request.AuthLogin;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


    public AuthResponse login(AuthLogin authLogin) {
        String email = authLogin.getEmail();

        Credentials foundUser = credentialsRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Account with email [" + email + "] does not exist"));

        if (!passwordEncoder.matches(authLogin.getPassword(), foundUser.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        authenticateUser(authLogin);

        String jwtToken = generateJwtToken(foundUser);
        return new AuthResponse(jwtToken);
    }

    private void authenticateUser(AuthLogin authLogin) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(authLogin.getEmail(),
                authLogin.getPassword());
        var authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String generateJwtToken(Credentials credentials) {
        if (credentials.getRole().equals(SPONSOR)) {
            return jwtTokenProvider.generateJwtToken(
                    credentials.getEmail(),
                    credentials.getSponsor().getId(),
                    credentials.getRole(),
                    credentials.getSponsor().getFullname()
            );
        } else {
            return jwtTokenProvider.generateJwtToken(
                    credentials.getEmail(),
                    credentials.getTalent().getId(),
                    credentials.getRole(),
                    credentials.getTalent().getFirstname()
            );
        }
    }
}
