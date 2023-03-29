package com.uptalent.mapper;

import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    List<TalentGeneralInfo> toTalentDTOs(List<Talent> talents);
    TalentProfile toTalentProfileDTO(Talent talent);
    TalentOwnProfile toTalentOwnProfileDTO(Talent talent);
}
