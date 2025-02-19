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


    @Test
    @DisplayName("계정 설정 뷰")
    @WithAccount("shark")
    void testGetAccountToUpdate() throws Exception {
        mockMvc.perform(get("/settings/account").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("닉네임 수정 성공")
    @WithAccount("shark")
    void testUpdateAccountNicknameSuccess() throws Exception {
        String newNickname = "sharkNew";
        // POST 요청 시 form 데이터로 보내는 필드 이름은 NicknameForm의 프로퍼티명과 일치해야 합니다.
        // 여기서는 form의 필드명이 "nickname"이라고 가정합니다.
        mockMvc.perform(post("/settings/account")
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(flash().attribute("message", "닉네임을 수정했습니다."));

        // DB에서 변경된 계정을 조회하여 닉네임이 업데이트 되었는지 확인합니다.
        Account updated = accountRepository.findByNickname(newNickname);
        assertNotNull(updated, "새로운 닉네임으로 계정이 존재해야 합니다.");
    }

    @Test
    @DisplayName("계정 삭제 성공")
    @WithAccount("shark")
    void testDeleteAccount() throws Exception {
        // deleteAccount 요청 전, 계정이 존재하는지 확인
        Account accountBefore = accountRepository.findByNickname("shark");
        assertNotNull(accountBefore, "삭제 전 계정은 존재해야 합니다.");

        mockMvc.perform(post("/settings/account/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // 삭제 후 DB에서 계정을 조회하여 null이어야 합니다.
        Account deleted = accountRepository.findByNickname("shark");
        assertNull(deleted, "계정 삭제 후 계정이 존재하면 안 됩니다.");
    }
}