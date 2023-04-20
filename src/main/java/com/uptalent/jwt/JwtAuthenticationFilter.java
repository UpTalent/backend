package com.uptalent.jwt;

import com.uptalent.principal.TalentDetailService;
import com.uptalent.talent.repository.TalentRepository;
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

import static com.uptalent.jwt.JwtConstant.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final TalentRepository talentRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        /* check if header with jwt-token exists */
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_HEADER)) {

            String jwtToken = authorizationHeader.substring(TOKEN_HEADER.length());
            String email = jwtTokenProvider.getSubject(jwtToken);

            /* check if token valid */
            if (jwtTokenProvider.isTokenValid(email, jwtToken) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                Long id = jwtTokenProvider.getId(jwtToken);

                if (talentRepository.findById(id).isPresent()) {
                    GrantedAuthority authority = jwtTokenProvider.getAuthority(jwtToken);
                    Authentication authentication = jwtTokenProvider.getAuthentication(id, authority, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
