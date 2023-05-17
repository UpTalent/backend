package com.uptalent.skill.service;

import com.uptalent.mapper.SkillMapper;
import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public List<SkillTalentInfo> getAllSkills() {
        return skillMapper.toSkillInfos(skillRepository.findAll());
    }
}
