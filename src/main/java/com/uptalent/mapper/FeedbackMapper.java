package com.uptalent.mapper;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.answer.model.response.FeedbackInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    List<FeedbackInfo> toFeedbackInfos(List<Answer> answers);
    FeedbackInfo toFeedbackInfo(Answer answer);
}