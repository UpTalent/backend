package com.uptalent.mapper;

import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    List<TalentDTO> toTalentDTOs(List<Talent> talents);
    TalentProfileDTO toTalentProfileDTO(Talent talent);
    TalentOwnProfileDTO toTalentOwnProfileDTO(Talent talent);
}
