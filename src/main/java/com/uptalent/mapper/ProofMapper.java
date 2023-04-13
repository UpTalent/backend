package com.uptalent.mapper;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProofMapper {
    ProofDetailInfo toProofDetailInfo(Proof proof);
    default Proof toProof(ProofModify proofModify) {
        return Proof.builder()
                .iconNumber(proofModify.getIconNumber())
                .title(proofModify.getTitle())
                .summary(proofModify.getSummary())
                .content(proofModify.getContent())
                .status(ProofStatus.valueOf(proofModify.getStatus()))
                .build();
    }
    List<ProofGeneralInfo> toProofGeneralInfos(List<Proof> content);

    List<ProofDetailInfo> toProofDetailInfos(List<Proof> content);
}
