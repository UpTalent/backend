package com.uptalent.talent;

import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;
    private final PasswordEncoder passwordEncoder;

    public PageWithMetadata<TalentDTO> getAllTalents(int page, int size){
        Page<Talent> talentPage = talentRepository.findAll(PageRequest.of(page, size));
        List<TalentDTO> talentDTOs = talentMapper.toTalentDTOs(talentPage.getContent());
        return new PageWithMetadata<>(talentDTOs,
                talentPage.getNumber(),
                talentPage.getSize(),
                talentPage.getTotalPages());
    }

    @Transactional
    public void addTalent(TalentRegistrationRequest talent){
        if(talentRepository.existsByEmailIgnoreCase(talent.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user has already been created");
        }
        else
            talentRepository.save(Talent.builder()
                    .password(passwordEncoder.encode(talent.getPassword()))
                    .email(talent.getEmail())
                    .firstname(talent.getFirstName())
                    .lastname(talent.getLastName())
                    .skills(new LinkedHashSet<>(talent.getSkills()))
                    .build());
    }
}
