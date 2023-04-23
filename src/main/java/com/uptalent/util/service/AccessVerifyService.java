package com.uptalent.util.service;

import com.uptalent.credentials.model.enums.Role;
import com.uptalent.talent.exception.DeniedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

@Service
public class AccessVerifyService {

    public boolean isPersonalProfile(Long requiredId, Role requiredRole) {
        Long principalId = getPrincipalId();
        return Objects.equals(requiredId, principalId) && hasRole(requiredRole);
    }

    public void tryGetAccess(Long requiredId, Role requiredRole, String errorMessage) {
        if (!isPersonalProfile(requiredId, requiredRole))
            throw new DeniedAccessException(errorMessage);
    }

    public Long getPrincipalId() {
        String id = String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return (!Objects.equals(id, "anonymousUser")) ? Long.parseLong(id) : 0L;
    }

    public boolean hasRole(Role requiredRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(requiredRole.name()));
    }
}
