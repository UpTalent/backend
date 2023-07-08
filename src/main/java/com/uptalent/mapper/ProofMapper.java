package com.uptalent.mapper;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.*;
import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.util.model.response.Author;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProofMapper {
    default ProofDetailInfo toProofDetailInfo(Proof proof) {
        return new ProofDetailInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getContent(),
                proof.getPublished(),
                proof.getKudos(),
                proof.getStatus(),
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet())
        );
    }
    default Proof toProof(ProofModify proofModify) {
        return Proof.builder()
                .iconNumber(proofModify.getIconNumber())
                .title(proofModify.getTitle())
                .summary(proofModify.getSummary())
                .content(proofModify.getContent())
                .status(ContentStatus.valueOf(proofModify.getStatus()))
                .build();
    }

    default Page<ProofGeneralInfo> toProofGeneralInfos(Page<Proof> proofs) {
        return proofs.map(this::toProofGeneralInfo);
    }

    default ProofGeneralInfo toProofGeneralInfo(Proof proof) {
        return new ProofGeneralInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getKudos(),
                proof.getPublished(),
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet()),
                toAuthor(proof.getTalent())
        );
    }

    default ProofTalentGeneralInfo toProofTalentGeneralInfo(Proof proof, Boolean isMyProof) {
        return new ProofTalentGeneralInfo(
                proof.getId(),
                proof.getIconNumber(),
                proof.getTitle(),
                proof.getSummary(),
                proof.getKudos(),
                proof.getPublished(),
                isMyProof,
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet()),
                toAuthor(proof.getTalent())
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
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet()),
                toAuthor(proof.getTalent())
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
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet())
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
                proof.getSkillKudos().stream()
                        .map(sk ->new SkillProofInfo(sk.getSkill().getId(),sk.getSkill().getName(), sk.getKudos()))
                        .collect(Collectors.toSet())
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
                .collect(Collectors.toList());
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

    default Author toAuthor(Talent talent){
        return Author.builder()
                .id(talent.getId())
                .name(talent.getFirstname() + " " + talent.getLastname())
                .avatar(talent.getAvatar())
                .build();
    }
}
