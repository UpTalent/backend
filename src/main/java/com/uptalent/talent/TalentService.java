package com.uptalent.talent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentService {
    private final TalentRepository talentRepository;

    public List<Talent> getAllTalents(){
        return talentRepository.findAll();
    }
}
