package com.uptalent.bootstrapdata;

import com.github.javafaker.Faker;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.skill.repository.SkillKudosRepository;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
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
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final SkillRepository skillRepository;
    private final SkillKudosRepository skillKudosRepository;
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
        generateOneSponsor();
    }

    private Talent generateOneTalent() {
        String lastname = faker.name().lastName();
        String firstname = faker.name().firstName();
        String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@gmail.com";
        String location = faker.address().country() + ", " + faker.address().cityName();
        String password = "1234567890";

        Credentials credentials = Credentials.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .verified(true)
                .build();
        credentialsRepository.save(credentials);

        return Talent.builder()
                .credentials(credentials)
                .lastname(lastname)
                .firstname(firstname)
                .avatar(faker.avatar().image())
                .banner(faker.internet().image())
                .birthday(LocalDate.now())
                .aboutMe(faker.lebowski().quote())
                .location(location)
                .skills(generateSkills())
                .build();
    }

    private Proof generateOneProof(Talent talent) {
        Skill skill = Skill.builder()
                .name(faker.job().keySkills())
                .build();
        skillRepository.save(skill);
        SkillKudos skillKudos = SkillKudos.builder()
                .skill(skill)
                .build();
        skillKudosRepository.save(skillKudos);
        skill.setSkillKudos(new HashSet<>(List.of(skillKudos)));
        skillRepository.save(skill);

        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Proof proof = Proof.builder()
                .iconNumber(faker.random().nextInt(11))
                .title("Proof of " + talent.getFirstname() + " " + talent.getLastname())
                .summary("Summary of " + talent.getFirstname() + " " + talent.getLastname())
                .content(faker.lorem().paragraph())
                .status(ContentStatus.PUBLISHED)
                .published(faker.date().past(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .talent(talent)
                .skillKudos(new HashSet<>(List.of(skillKudos)))
                .build();
        proofRepository.save(proof);
        return proof;
    }


        private Set<Skill> generateSkills() {

        Set<Skill> skills = new HashSet<>();
        int size = faker.random().nextInt(3) + 3;

        for (int i = 0; i < size; i++) {
            Skill skill = Skill.builder()
                    .name(faker.job().keySkills())
                    .build();
            skills.add(skill);
        }
        return skills;
    }
    private Sponsor generateOneSponsor() {
        String fullname = "Andrii";
        String email = "andrii@gmail.com";
        String password = "1234567890";

        Credentials credentials = Credentials.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(AccountStatus.ACTIVE)
                .role(Role.SPONSOR)
                .verified(true)
                .build();
        credentials = credentialsRepository.save(credentials);
        Sponsor sponsor = Sponsor.builder()
                .credentials(credentials)
                .fullname(fullname)
                .avatar(faker.avatar().image())
                .kudos(50)
                .build();
        sponsor = sponsorRepository.save(sponsor);
        credentials.setSponsor(sponsor);
        return sponsor;
    }


}
