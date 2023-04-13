package com.uptalent.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.uptalent.principal.Role;
import com.uptalent.talent.model.entity.Talent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.uptalent.jwt.JwtConstant.*;

/**
 *  Jwt provider for working with jwt-token
 *
 * @version 1.0
 * @author Dmytro Teliukov
 *
 * */
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generate JWT-token for authorization our talent
     * P.S.: In the future when we will have more than 1 role, we should change ROLE_CLAIM and add parameter role
     *
     * @param talent Talent
     *
     * @return jwt token
     * */
    public String generateJwtToken(Talent talent) {
        return JWT.create()
                .withIssuer(TOKEN_ISSUE)
                .withAudience()
                .withIssuedAt(new Date())
                .withSubject(talent.getEmail())
                .withClaim(TALENT_ID_CLAIM, talent.getId())
                .withClaim(ROLE_CLAIM, Role.TALENT.name())
                .withClaim(FIRSTNAME_CLAIM, talent.getFirstname())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    /**
     * Create authentication for filter authentication
     *
     * @param id Talent id
     * @param authority Talent authority
     * @param request Request for filter
     *
     * @return jwt token
     * */
    public Authentication getAuthentication(Long id, GrantedAuthority authority, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(id, null, List.of(authority));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    /**
     * Check if jwt-token is valid
     *
     * @param email Talent email
     * @param token JWT-Token
     *
     * @return is expired token
     * */
    public boolean isTokenValid(String email, String token) {
        return StringUtils.isNotEmpty(email) &&
                !isTokenExpired(token) &&
                getSubject(token).equals(email);
    }

    /**
     * Get subject(email) from jwt-token
     *
     * @param token JWT-Token
     *
     * @return email
     * */
    public String getSubject(String token) {
        return JWT.decode(token).getSubject();
    }

    public Long getId(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getClaim("talent_id").asLong();
    }

    /**
     * Check if jwt-token is expired
     *
     * @param token JWT-Token
     *
     * @return is expired token
     * */
    private boolean isTokenExpired(String token) {
        Date expiration = JWT.decode(token).getExpiresAt();
        return expiration.before(new Date());
    }

    /**
     * Get authority(role) from jwt-token
     *
     * @param token JWT-Token
     *
     * @return authority
     * */
    public GrantedAuthority getAuthority(String token) {
        return new SimpleGrantedAuthority(getRoleFromToken(token));
    }


    /**
     * Parse role from jwt-token
     *
     * @param token JWT-Token
     *
     * @return role
     * */
    private String getRoleFromToken(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getClaim(ROLE_CLAIM).asString();
    }

    /**
     * Get verifier for parsing jwt-token
     *
     * @return jwt-verifier
     * */
    private JWTVerifier getVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(TOKEN_ISSUE).build();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException(TOKEN_NOT_VERIFIED_MESSAGE);
        }
        return verifier;
    }
}
