package com.uptalent.talent;

import com.uptalent.mapper.TalentMapper;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TalentServiceTest {

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private TalentMapper talentMapper;

    @InjectMocks
    private TalentService talentService;

    private Talent talent;

    private static final Long nonExistentTalentId = 1000L;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("Teliukov")
                .firstname("Dmytro")
                .email("dmytro.teliukov@gmail.com")
                .password("12345")
                .skills(Set.of("Java", "Spring"))
                .build();

    }

    @Test
    @Order(2)
    @DisplayName("[US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() {
        securitySetUp();

        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));
        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn("john.doe@gmail.com");

        when(talentMapper.toTalentProfileDTO(any()))
                .thenReturn(new TalentProfileDTO());

        TalentProfileDTO talentProfile = talentService.getTalentProfileById(talent.getId());

        assertThat(talentProfile).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("[US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() {
        securitySetUp();

        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));
        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn(talent.getEmail());


        when(talentMapper.toTalentOwnProfileDTO(any()))
                .thenReturn(new TalentOwnProfileDTO(talent.getEmail(), new Date()));

        TalentOwnProfileDTO ownProfile = ((TalentOwnProfileDTO) talentService.getTalentProfileById(talent.getId()));

        assertThat(ownProfile).isNotNull();
        assertThat(ownProfile.getEmail()).isEqualTo(talent.getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("[US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {

        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.getTalentProfileById(nonExistentTalentId));
    }

    @Test
    @Order(13)
    @DisplayName("[US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() {
        securitySetUp();

        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));
        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn(talent.getEmail());

        willDoNothing().given(talentRepository).delete(talent);

        talentService.deleteTalent(talent.getId());

        verify(talentRepository, times(1)).delete(talent);
    }

    @Test
    @Order(14)
    @DisplayName("[US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() {
        securitySetUp();

        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));
        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn("john.doe@gmail.com");

        assertThrows(DeniedAccessException.class, () -> talentService.deleteTalent(talent.getId()));

    }

    @Test
    @Order(15)
    @DisplayName("[US-4] - Delete non-existent profile")
    void deleteNonExistentProfile() {
        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.deleteTalent(nonExistentTalentId));
    }


    private void securitySetUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}