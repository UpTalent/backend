package com.uptalent.sponsor.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SponsorRating {
    private String fullname;
    private String avatar;
    private Long totalSumKudos;
}
