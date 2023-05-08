package com.uptalent.config;

import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final FileStoreService fileStoreService;

    @Async
    @Scheduled(cron = "* */5 * * * ?")
    //@Scheduled(cron = "* 10 11 * * ?")
    @Transactional
    public void deletePermanently(){
        List<Sponsor> sponsors = sponsorRepository.findSponsorsToPermanentDelete(LocalDateTime.now());
        sponsors.forEach(s -> fileStoreService.deleteImageByUserIdAndRole(s.getId(), s.getCredentials().getRole()));
        sponsorRepository.updateSponsorDeleteData(sponsors.stream().map(Sponsor::getId).collect(Collectors.toList()));
        credentialsRepository.updateCredentialsDeleteData(sponsors.stream().map(s -> s.getCredentials().getId()).collect(Collectors.toList()));
    }
}

