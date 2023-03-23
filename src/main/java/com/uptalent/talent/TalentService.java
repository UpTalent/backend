package com.uptalent.talent;

import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.exception.TalentExistsException;
import com.uptalent.talent.exception.TalentNoFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.request.TalentLoginRequest;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import com.uptalent.talent.model.response.TalentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public PageWithMetadata<TalentDTO> getAllTalents(int page, int size){
        Page<Talent> talentPage = talentRepository.findAll(PageRequest.of(page, size));
        List<TalentDTO> talentDTOs = talentMapper.toTalentDTOs(talentPage.getContent());
        return new PageWithMetadata<>(talentDTOs,
                talentPage.getNumber(),
                talentPage.getSize(),
                talentPage.getTotalPages());
    }

    @Transactional
    public TalentResponse addTalent(TalentRegistrationRequest talent){
        if (talentRepository.existsByEmailIgnoreCase(talent.getEmail())){
            throw new TalentExistsException("The talent has already exists with email [" + talent.getEmail() + "]");
        }

        var savedTalent = talentRepository.save(Talent.builder()
                    .password(passwordEncoder.encode(talent.getPassword()))
                    .email(talent.getEmail())
                    .firstname(talent.getFirstname())
                    .lastname(talent.getLastname())
                    .skills(new LinkedHashSet<>(talent.getSkills()))
                    .build());

        String jwtToken = jwtTokenProvider.generateJwtToken(savedTalent.getEmail());
        return new TalentResponse(savedTalent.getId(), jwtToken);
    }

    @Transactional
    public TalentResponse login(TalentLoginRequest loginRequest) {
        String email = loginRequest.email();
        Talent foundTalent = talentRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new TalentNoFoundException("Talent was not found by email [" + email + "]"));

        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateJwtToken(email);
        return new TalentResponse(foundTalent.getId(), jwtToken);
    }

    public TalentProfileDTO getTalentProfileById(Long talentId) {
        Talent foundTalent = talentRepository.findById(talentId)
                .orElseThrow(() -> new TalentNoFoundException("Talent was not found"));

        String authEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isPersonalProfile = authEmail.equalsIgnoreCase(foundTalent.getEmail());

        var talentProfile = talentMapper.toTalentProfileDTO(foundTalent);
        talentProfile.setPersonalProfile(isPersonalProfile);

        return talentProfile;
    }
}
