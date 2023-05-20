package com.uptalent.mapper;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.skill.model.SkillInfo;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KudosHistoryMapper {
    default KudosedProofHistory toKudosedProofHistory(KudosHistory kudosHistory) {
        return new KudosedProofHistory(
                kudosHistory.getSent(),
                kudosHistory.getTotalKudos(),
                kudosHistory.getSkillKudosHistories().stream()
                        .map(sk ->new SkillInfo(sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet())
        );
    }

    default KudosSender toKudosedSender(KudosHistory kudosHistory) {
        return new KudosSender(
                kudosHistory.getSponsor().getFullname(),
                kudosHistory.getSponsor().getAvatar(),
                kudosHistory.getSent(),
                kudosHistory.getTotalKudos(),
                kudosHistory.getSkillKudosHistories().stream()
                        .map(sk ->new SkillInfo(sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet())
        );
    }
}
