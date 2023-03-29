package com.uptalent.talent.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentOwnProfile extends TalentProfile {
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
