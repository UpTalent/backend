package com.uptalent.bootstrapdata;

import com.github.javafaker.Faker;
import com.uptalent.talent.Talent;
import com.uptalent.talent.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TalentDataLoader implements CommandLineRunner {

    public static final int SIZE = 20;
    private final TalentRepository talentRepository;

    @Override
    public void run(String... args) {
        Faker faker = new Faker();

        for (int i = 0; i < SIZE; i++) {
            talentRepository.save(generateOneTalent(faker));
        }
    }

    private Talent generateOneTalent(Faker faker) {
        return Talent.builder()
                .lastname(faker.name().lastName())
                .firstname(faker.name().firstName())
                .photo(null)
                .banner(null)
                .skills(List.of("Java", "React", "Js"))
                .build();
    }
}
