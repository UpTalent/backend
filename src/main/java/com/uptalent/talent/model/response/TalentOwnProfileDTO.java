package com.uptalent.talent.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentOwnProfileDTO extends TalentProfileDTO {
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
}
