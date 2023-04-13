package com.uptalent.bootstrapdata;

import com.github.javafaker.Faker;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class FakeDataLoader implements CommandLineRunner {

    public static final int SIZE = 20;
    private final TalentRepository talentRepository;
    private final ProofRepository proofRepository;
    private final Faker faker;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public void run(String... args) {
        log.info("Bucket name is blank?:{}", env.getProperty("aws.bucket.name").isBlank());
        log.info("Region is blank?:{}", env.getProperty("aws.bucket.region").isBlank());
        log.info("Access key is blank?:{}", env.getProperty("aws.bucket.access-key").isBlank());
        log.info("Secret key is blank?:{}", env.getProperty("aws.bucket.secret-key").isBlank());

        for (int i = 0; i < SIZE; i++) {
            Talent talent = generateOneTalent();
            talentRepository.save(talent);

            Proof proof = generateOneProof(talent);
            Proof anotherProof = generateOneProof(talent);
            talent.setProofs(List.of(proof, anotherProof));

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
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Proof proof = Proof.builder()
                .iconNumber(faker.random().nextInt(11))
                .title("Proof of " + talent.getFirstname() + " " + talent.getLastname())
                .summary("Summary of " + talent.getFirstname() + " " + talent.getLastname())
                .content(faker.lorem().paragraph())
                .status(ProofStatus.PUBLISHED)
                .published(faker.date().past(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
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
