package com.uptalent.answer.service;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.answer.model.enums.MessageStatus;
import com.uptalent.answer.model.request.TemplateMessageRequest;
import com.uptalent.answer.repository.AnswerRepository;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.util.service.AccessVerifyService;
import com.uptalent.vacancy.submission.exception.InvalidContactInfoException;
import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static com.uptalent.util.RegexValidation.*;
import static com.uptalent.util.RegexValidation.isValidLinkedInUrl;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AccessVerifyService accessVerifyService;
    private final SponsorRepository sponsorRepository;
    @PreAuthorize("hasAuthority('SPONSOR')")
    @Transactional
    public void createTemplate(TemplateMessageRequest templateMessageRequest) {
        Long sponsorId = accessVerifyService.getPrincipalId();

        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));

        validateContactInfo(templateMessageRequest.getContactInfo());

        var answer = Answer.builder()
                .contactInfo(templateMessageRequest.getContactInfo())
                .isTemplatedMessage(templateMessageRequest.getIsTemplatedMessage())
                .status(MessageStatus.valueOf(templateMessageRequest.getStatus()))
                .title(templateMessageRequest.getTitle())
                .message(templateMessageRequest.getMessage())
                .sponsor(sponsor)
                .build();

        answerRepository.save(answer);
    }
}
