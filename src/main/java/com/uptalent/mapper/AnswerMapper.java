package com.uptalent.mapper;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.answer.model.response.AnswerInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    List<AnswerInfo> toAnswerInfos(List<Answer> answers);
}