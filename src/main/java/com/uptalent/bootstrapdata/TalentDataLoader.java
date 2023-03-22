package com.uptalent.bootstrapdata;

import com.github.javafaker.Faker;
import com.uptalent.talent.Talent;
import com.uptalent.talent.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TalentDataLoader implements CommandLineRunner {

    public static final int SIZE = 20;
    private final TalentRepository talentRepository;
    private final Faker faker;

    @Override
    public void run(String... args) {
        for (int i = 0; i < SIZE; i++) {
            talentRepository.save(generateOneTalent());
        }
    }

    private Talent generateOneTalent() {
        String lastname = faker.name().lastName();
        String firstname = faker.name().firstName();
        String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@gmail.com";
        String location = faker.address().country() + ", " + faker.address().cityName();

        return Talent.builder()
                .lastname(lastname)
                .firstname(firstname)
                .photo(faker.avatar().image())
                .banner(faker.internet().image())
                .email(email)
                .password(faker.internet().password())
                .birthday(faker.date().birthday())
                .aboutMe(faker.lebowski().quote())
                .location(location)
                .skills(generateSkills())
                .build();
    }

    private Set<String> generateSkills() {
        Set<String> skills = new HashSet<>();
        int size = faker.random().nextInt(3) + 3;

        for (int i = 0; i < size; i++)
            skills.add(faker.job().keySkills());

        return skills;
    }
}
