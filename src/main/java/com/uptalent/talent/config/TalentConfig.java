package com.uptalent.talent.config;

import com.uptalent.talent.model.property.TalentAgeRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TalentConfig {
    @Value("${talent.min-age}")
    private int TALENT_MIN_AGE;

    @Value("${talent.max-age}")
    private int TALENT_MAX_AGE;

    private static final String AGE_ERROR_MESSAGE = "Age of talent should be greater than %d and less than %d";

    @Bean
    public TalentAgeRange talentAgeRange() {
        String errorMessage = String.format(AGE_ERROR_MESSAGE, TALENT_MIN_AGE, TALENT_MAX_AGE);
        return new TalentAgeRange(TALENT_MIN_AGE, TALENT_MAX_AGE, errorMessage);
    }
}
