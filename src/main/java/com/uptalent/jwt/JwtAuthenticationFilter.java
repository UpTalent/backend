package com.uptalent.jwt;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.repository.CredentialsRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.uptalent.credentials.model.enums.AccountStatus.ACTIVE;
import static com.uptalent.jwt.JwtConstant.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CredentialsRepository credentialsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        /* check if header with jwt-token exists */
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_HEADER)) {

            String jwtToken = authorizationHeader.substring(TOKEN_HEADER.length());

            /* check if token valid */
            if (jwtTokenProvider.isTokenValid(jwtToken) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtTokenProvider.getSubject(jwtToken);

                Long id = jwtTokenProvider.getId(jwtToken);
                Optional<Credentials> credentials = credentialsRepository.findByEmailIgnoreCase((email));

                if (credentials.isPresent() && credentials.get().getStatus().equals(ACTIVE)) {
                    GrantedAuthority authority = jwtTokenProvider.getAuthority(jwtToken);
                    Authentication authentication = jwtTokenProvider.getAuthentication(id, authority, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
