package com.uptalent.util.service;

import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessVerifyService {
    private final TalentRepository talentRepository;

    public boolean isPersonalProfile(Long talentId) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return talentRepository.verifyTalent(talentId, email);
    }

    public void tryGetAccess(Long talentId, String errorMessage) {
        if (!isPersonalProfile(talentId))
            throw new DeniedAccessException(errorMessage);
    }
}
