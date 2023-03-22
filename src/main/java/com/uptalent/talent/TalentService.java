package com.uptalent.talent;

import com.uptalent.mapper.TalentMapper;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.res.TalentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;

    public Page<TalentDTO> getAllTalents(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Talent> talentPage = talentRepository.findAll(pageable);
        List<TalentDTO> talentDTOs = talentMapper.toTalentDTOs(talentPage.getContent());
        return new PageImpl<>(talentDTOs, pageable, talentPage.getTotalElements());
    }
}
