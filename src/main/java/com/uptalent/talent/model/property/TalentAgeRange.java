package com.uptalent.talent.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TalentAgeRange {
    private int minAge;
    private int maxAge;
    private String errorMessage;
}
