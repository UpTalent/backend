package com.uptalent.mapper;

import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.proof.kudos.model.response.KudosedProofInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KudosHistoryMapper {
    KudosedProofInfo toKudosedProofInfo(KudosedProof kudosedProof);
    List<KudosedProofHistory> toKudosedProofHistories(List<KudosedProof> kudosedProofs);
}
