    package com.uptalent.proof.model.response;

    import com.uptalent.proof.model.enums.ProofStatus;
    import com.uptalent.skill.model.SkillProofInfo;
    import lombok.Data;
    import lombok.EqualsAndHashCode;

    import java.time.LocalDateTime;
    import java.util.Set;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class ProofSponsorDetailInfo extends ProofDetailInfo {
        private long sumKudosFromMe;

        public ProofSponsorDetailInfo(Long id, Integer iconNumber, String title, String summary, String content,
                                      LocalDateTime published, int kudos, ProofStatus status, long sumKudosFromMe,
                                      Set<SkillProofInfo> skills) {
            super(id, iconNumber, title, summary, content, published, kudos, status, skills);
            this.sumKudosFromMe = sumKudosFromMe;
        }
    }
