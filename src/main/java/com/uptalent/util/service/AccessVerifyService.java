package com.uptalent.util.service;

import com.uptalent.talent.exception.DeniedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessVerifyService {

    public boolean isPersonalProfile(Long talentId) {
        Long id = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Objects.equals(talentId, id);
    }

    public void tryGetAccess(Long talentId, String errorMessage) {
        if (!isPersonalProfile(talentId))
            throw new DeniedAccessException(errorMessage);
    }
}
