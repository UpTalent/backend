package com.uptalent.sponsor.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SponsorProfile {
    private Long id;
    private String fullname;
    private String avatar;
    private String email;
    private long kudos;

}
