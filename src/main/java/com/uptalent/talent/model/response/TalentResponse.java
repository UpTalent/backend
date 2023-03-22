package com.uptalent.talent.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TalentResponse(@JsonProperty("talent_id") long talentId,
                             @JsonProperty("jwt_token") String jwtToken) {
}
