package com.uptalent.talent.model.response;


public record TalentResponse(long talentId,
                             String jwtToken) {
    public long getTalentId() {
        return talentId;
    }
}
