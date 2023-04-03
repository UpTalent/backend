package com.uptalent.bootstrapdata;

import com.github.javafaker.Faker;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.entity.ProofStatus;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TalentDataLoader implements CommandLineRunner {

    public static final int SIZE = 20;
    private final TalentRepository talentRepository;
    private final ProofRepository proofRepository;
    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (int i = 0; i < SIZE; i++) {
            Talent talent = generateOneTalent();
            talentRepository.save(talent);

            Proof proof = generateOneProof(talent);
            talent.setProofs(List.of(proof));

            talentRepository.save(talent);
        }
    }

    private Talent generateOneTalent() {
        String lastname = faker.name().lastName();
        String firstname = faker.name().firstName();
        String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@gmail.com";
        String location = faker.address().country() + ", " + faker.address().cityName();
        String password = "1234567890";

        return Talent.builder()
                .lastname(lastname)
                .firstname(firstname)
                .avatar(faker.avatar().image())
                .banner(faker.internet().image())
                .email(email)
                .password(passwordEncoder.encode(password))
                .birthday(LocalDate.now())
                .aboutMe(faker.lebowski().quote())
                .location(location)
                .skills(generateSkills())
                .build();
    }

    private Proof generateOneProof(Talent talent) {
        Proof proof = Proof.builder()
                .title("Proof of " + talent.getFirstname() + " " + talent.getLastname())
                .content(faker.lorem().paragraph())
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .talent(talent)
                .build();
        return proofRepository.save(proof);
    }

    private Set<String> generateSkills() {
        Set<String> skills = new HashSet<>();
        int size = faker.random().nextInt(3) + 3;

        for (int i = 0; i < size; i++)
            skills.add(faker.job().keySkills());

        return skills;
    }
}
