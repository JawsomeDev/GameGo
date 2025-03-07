package com.gamego.controller;

import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.form.RoomDescriptionForm;
import com.gamego.email.EmailService;
import com.gamego.factory.AccountFactory;
import com.gamego.factory.RoomFactory;
import com.gamego.repository.account.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomSettingsControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;
    @Autowired
    private AccountFactory accountFactory;
    @Autowired
    private RoomFactory roomFactory;
    @Autowired
    private AccountRepository accountRepository;


    @Test
    @WithAccount("shark")
    @DisplayName("방 소개 수정 조회 실패 (권한x)")
    void updateDescriptionForm_fail() throws Exception {
        Account test = accountFactory.createAccount("test");
        Room room = roomFactory.createRoom("test-room", test);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 소개 수정 조회 성공")
    void updateDescriptionForm_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/settings/description"))
                .andExpect(model().attributeExists("roomDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);


        mockMvc.perform(post( "/room/" + room.getEncodedPath() + "/settings/description")
                .param("shortDescription", "short description")
                .param("longDescription", "long description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/description"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/description")
                .param("shortDescription", "")
                .param("longDescription", "")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("roomDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("배너 조회 - 성공")
    void updateBanner_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/settings/banner"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("배너 조회 - 실패(권한x)")
    void updateBanner_fail() throws Exception {
        Account test = accountFactory.createAccount("test");
        Room room = roomFactory.createRoom("test-room", test);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/banner"))
                .andExpect(status().isForbidden());
    }



    @Test
    @WithAccount("shark")
    @DisplayName("배너 사용하지 않기")
    void disableBanner_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/banner/disabled")
                        .with(csrf())
                .param("useBanner", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/banner"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("배너 사용하기")
    void enableBanner_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/banner/enabled")
                .with(csrf())
                .param("useBanner", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/banner"));

    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 게임 설정 조회")
    void updateGames_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/settings/games"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("games"));
    }







}