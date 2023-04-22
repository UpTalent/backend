package com.uptalent.mapper;

import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    List<TalentGeneralInfo> toTalentGeneralInfos(List<Talent> talents);
    TalentProfile toTalentProfile(Talent talent);
    default TalentOwnProfile toTalentOwnProfile(Talent talent){
        return new TalentOwnProfile(
                talent.getId(),
                talent.getLastname(),
                talent.getFirstname(),
                talent.getAvatar(),
                talent.getBanner(),
                talent.getSkills(),
                talent.getLocation(),
                talent.getAboutMe(),
                talent.getCredentials().getEmail(),
                talent.getBirthday()
        );
    }
}
