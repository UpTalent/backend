package com.uptalent.principal;

import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.talent.model.entity.Talent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalentDetailService implements UserDetailsService {

    private final TalentRepository talentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Talent talent = talentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Talent not found by email [ " + email + "]"));

        return new TalentPrincipal(talent);
    }
}
