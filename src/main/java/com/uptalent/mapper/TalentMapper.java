package com.uptalent.mapper;

import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.res.TalentDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    List<TalentDTO> toTalentDTOs(List<Talent> talents);
}