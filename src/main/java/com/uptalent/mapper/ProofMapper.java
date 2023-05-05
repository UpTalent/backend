package com.uptalent.mapper;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.*;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
                .skills(new HashSet<>())
                .build();
    }

    default Page<ProofGeneralInfo> toProofGeneralInfos(Page<Proof> proofs) {
        return proofs.map(this::toProofGeneralInfo);
    }

    ProofGeneralInfo toProofGeneralInfo(Proof proof);

    default ProofTalentGeneralInfo toProofTalentGeneralInfo(Proof proof, Boolean isMyProof) {
        return new ProofTalentGeneralInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getKudos(),
                proof.getPublished(),
                isMyProof,
                proof.getSkills()
        );
    }

    default ProofSponsorGeneralInfo toProofSponsorGeneralInfo(Proof proof, Long kudosSumFromMe) {
        return new ProofSponsorGeneralInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getKudos(),
                proof.getPublished(),
                kudosSumFromMe,
                proof.getSkills()
        );
    }

    default ProofSponsorDetailInfo toProofSponsorDetailInfo(Proof proof, Long kudosSumFromMe) {
        return new ProofSponsorDetailInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getContent(),
                proof.getPublished(),
                proof.getKudos(),
                proof.getStatus(),
                kudosSumFromMe,
                proof.getSkills()
        );
    }

    default ProofTalentDetailInfo toProofTalentDetailInfo(Proof proof, Boolean isMyProof) {
        return new ProofTalentDetailInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getContent(),
                proof.getPublished(),
                proof.getKudos(),
                proof.getStatus(),
                isMyProof,
                proof.getSkills()
        );
    }

    default Page<ProofTalentGeneralInfo> toProofTalentGeneralInfos(Page<Object[]> proofsAndIsMyProofList,
                                                                   Pageable pageRequest) {
        List<ProofTalentGeneralInfo> proofTalentGeneralInfos = proofsAndIsMyProofList.getContent().stream()
                .map(tuple -> {
                    Proof proof = (Proof) tuple[0];
                    Boolean isMyProof = (Boolean) tuple[1];
                    return toProofTalentGeneralInfo(proof, isMyProof);
                })
                .collect(Collectors.toList());;
        return new PageImpl<>(proofTalentGeneralInfos, pageRequest, proofsAndIsMyProofList.getTotalElements());
    }

    default Page<ProofSponsorGeneralInfo> toProofSponsorGeneralInfos(Page<Object[]> proofsAndKudosSum,
                                                            Pageable pageRequest){
        List<ProofSponsorGeneralInfo> proofTalentGeneralInfos = proofsAndKudosSum.getContent().stream()
                .map(tuple -> {
                    Proof proof = (Proof) tuple[0];
                    Long kudosSumFromMe = (Long) tuple[1];
                    return toProofSponsorGeneralInfo(proof, kudosSumFromMe);
                })
                .collect(Collectors.toList());
        return new PageImpl<>(proofTalentGeneralInfos, pageRequest, proofsAndKudosSum.getTotalElements());
    }

    default Page<ProofSponsorDetailInfo> toProofSponsorDetailInfos(Page<Object[]> talentProofs,
                                                           Pageable pageRequest){
        List<ProofSponsorDetailInfo> proofSponsorDetailInfos = talentProofs.getContent().stream()
                .map(tuple -> {
                    Proof proof = (Proof) tuple[0];
                    Long kudosSumFromMe = (Long) tuple[1];
                    return toProofSponsorDetailInfo(proof, kudosSumFromMe);
                })
                .collect(Collectors.toList());
        return new PageImpl<>(proofSponsorDetailInfos, pageRequest, talentProofs.getTotalElements());
    }

    default Page<ProofTalentDetailInfo> toProofTalentDetailInfos(Page<Object[]> talentProofs,
                                                         Pageable pageRequest){
        List<ProofTalentDetailInfo> proofTalentDetailInfos = talentProofs.getContent().stream()
                .map(tuple -> {
                    Proof proof = (Proof) tuple[0];
                    Boolean isMyProof = (Boolean) tuple[1];
                    return toProofTalentDetailInfo(proof, isMyProof);
                })
                .collect(Collectors.toList());
        return new PageImpl<>(proofTalentDetailInfos, pageRequest, talentProofs.getTotalElements());
    }
}
