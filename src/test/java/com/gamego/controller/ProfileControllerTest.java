package com.gamego.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.account.dto.AccountForm;
import com.gamego.domain.account.dto.TimePreferenceForm;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.dto.GameForm;
import com.gamego.email.EmailService;
import com.gamego.repository.AccountRepository;
import com.gamego.repository.GameRepository;
import com.gamego.service.AccountService;
import jakarta.persistence.EntityManager;
import lombok.With;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;


    @BeforeEach
    void setUp() {
        if (!gameRepository.existsByName("Dota 2")) {
            Game dota2 = Game.builder()
                    .name("Dota 2")
                    .build();
            gameRepository.save(dota2);
        }
    }

    @Test
    @DisplayName("프로필 수정 뷰")
    @WithAccount("shark")
    void updateProfileForm() throws Exception {
        String bio = "ㅁㄴㅇㅁㄴㅇ뮈ㅏ우나ㅣ위무";
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("account"));
    }


    @Test
    @DisplayName("프로필 수정하기 - 성공")
    @WithAccount("shark")
    void updateProfile_success() throws Exception {
        String bio = "상어";

        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attribute("message", "프로필을 수정했습니다."));

        Account shark = accountRepository.findByNickname("shark");
        Assertions.assertThat(shark.getBio()).isEqualTo(bio);
    }

    @Test
    @DisplayName("프로필 수정하기 - 실패(이상한 값)")
    @WithAccount("shark")
    void updateProfile_fail() throws Exception {
        String url = "wwwwwwww.com";
        mockMvc.perform(post("/settings/profile")
                        .param("url", url)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());

        Account shark = accountRepository.findByNickname("shark");
        Assertions.assertThat(shark.getBio()).isNull();
    }

    @Test
    @DisplayName("패스워드 수정 뷰")
    @WithAccount("shark")
    void updatePasswordView() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }


    @Test
    @DisplayName("패스워드 수정 성공")
    @WithAccount("shark")
    void updatePassword_success() throws Exception {

        mockMvc.perform(post("/settings/password")
                .param("newPassword", "12121212")
                .param("newPasswordConfirm", "12121212")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));
        Account shark = accountRepository.findByNickname("shark");
        Assertions.assertThat(passwordEncoder.matches("12121212" , shark.getPassword())).isTrue();
    }

    @Test
    @DisplayName("패스워드 수정 실패")
    @WithAccount("shark")
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post("/settings/password")
                .with(csrf())
                .param("newPassword", "12121212")
                .param("newPasswordConfirm", "121212123"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
        Account shark = accountRepository.findByNickname("shark");
        Assertions.assertThat(passwordEncoder.matches("12121212" , shark.getPassword())).isFalse();
    }

    @Test
    @DisplayName("게임 태그 설정")
    @WithAccount("shark")
    void updateGamesForm() throws Exception {
        mockMvc.perform(get("/settings/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/games"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attributeExists("whitelist"));
    }


    @Test
    @DisplayName("게임 태그 추가")
    @WithAccount("shark")
    void updateGame_success() throws Exception {

        GameForm form = new GameForm();
        form.setGameName("Dota 2");

        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/settings/games/add")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("add")))
                .andExpect(jsonPath("$.gameTitle", is("Dota 2")));

    }

    @Test
    @DisplayName("게임 제거 성공")
    @WithAccount("shark")
    void removeGame_success() throws Exception {

        GameForm form = new GameForm();
        form.setGameName("Dota 2");

        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/settings/games/remove")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("remove")));
    }

    @Test
    @DisplayName("시간 선택 뷰")
    @WithAccount("shark")
    void testTimeToSelect() throws Exception {
        mockMvc.perform(get("/settings/times").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/times"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("timePreference", nullValue()))
                .andExpect(model().attributeExists("whitelist"));
    }

    @Test
    @DisplayName("시간대 추가 성공")
    @WithAccount("testuser")
    void testAddTimePreference() throws Exception {
        TimePreferenceForm form = new TimePreferenceForm();
        form.setTimePreference("오전");

        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/settings/times/add")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("add"))
                .andExpect(jsonPath("$.timePreference").value("오전"));

        Account account = accountRepository.findByNickname("testuser");
        Assertions.assertThat(account.getTimePreference()).isEqualTo(TimePreference.MORNING);
    }

    @Test
    @DisplayName("시간대 제거 성공")
    @WithAccount("testuser")
    void testRemoveTimePreference() throws Exception {
.
        TimePreferenceForm form = new TimePreferenceForm();
        form.setTimePreference("오전");
        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/settings/times/add")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/settings/times/remove")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("remove"));


        Account account = accountRepository.findByNickname("testuser");
        Assertions.assertThat(account.getTimePreference()).isNull();
    }
}