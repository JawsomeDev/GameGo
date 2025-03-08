package com.gamego.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.game.form.GameForm;
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
import org.springframework.http.MediaType;
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
    @Autowired
    private ObjectMapper objectMapper;


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
    @DisplayName("배너 이미지 수정 - 성공")
    void updateBannerImage_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/banner")
                .param("image", "something")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/banner"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("배너 기본값 사용 - 성공")
    void useDefaultBanner_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/banner/default")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/banner"))
                .andExpect(flash().attributeExists("message"));
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

    @Test
    @WithAccount("shark")
    @DisplayName("게임 추가 - 성공")
    void addGames_success() throws Exception{
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        GameForm gameForm = new GameForm();
        gameForm.setGameName("하스스톤");

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/games/add")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(gameForm)))
                .andExpect(status().isOk());
    }

    @Test
    @WithAccount("shark")
    @DisplayName("게임 제거 - 성공")
    void removeGames_success() throws Exception{
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        GameForm gameForm = new GameForm();
        gameForm.setGameName("하스스톤");

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/games/add")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(gameForm)));


        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/games/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(gameForm)))
                .andExpect(status().isOk());
    }
    @Test
    @WithAccount("shark")
    @DisplayName("게임 추가 - 실패(권한 없음)")
    void addGame_fail_forbidden() throws Exception {
        // 다른 유저가 만든 방
        Account test = accountFactory.createAccount("test");
        Room room = roomFactory.createRoom("test-room", test);

        GameForm gameForm = new GameForm();
        gameForm.setGameName("Elden Ring");

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/games/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(gameForm)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("shark")
    @DisplayName("시간대 설정 페이지 조회 - 성공")
    void updateTimes_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/times"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/settings/times"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("시간대 추가 - 성공")
    void addTimePreference_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        // 예: "MORNING"이라는 enum 값을 추가
        String jsonBody = "{\"timePreference\":\"MORNING\"}";

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/times/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithAccount("shark")
    @DisplayName("시간대 제거 - 성공")
    void removeTimePreference_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        String jsonBody = "{\"timePreference\":\"MORNING\"}";

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/times/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 기본정보 페이지 조회 - 성공")
    void updateRoomForm_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/settings/room"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 활성화 - 성공")
    void activeRoom_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        room.setRecruiting(false); // 모집 중이면 변경 불가 로직이 있을 수 있으므로

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/active")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 비활성화 - 성공")
    void closeRoom_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        room.setActive(true);
        room.setRecruiting(false);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/close")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("팀원 모집 시작 - 성공")
    void startRecruit_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        // room.canRecruitByTime()와 room.canRecruitByDay()가 true여야 성공

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/recruit/start")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("팀원 모집 종료 - 성공")
    void stopRecruit_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/recruit/stop")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 경로 수정 - 성공")
    void updateRoomPath_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/path")
                        .with(csrf())
                        .param("newPath", "new-path"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/new-path/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 이름 수정 - 성공")
    void updateRoomTitle_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/title")
                        .with(csrf())
                        .param("newTitle", "My New Room"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/settings/room"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("방 삭제 - 성공")
    void removeRoom_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/remove")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    // 아래와 같이 권한이 없는 경우(다른 유저가 만든 방)에 대한 테스트도 추가 가능
    @Test
    @WithAccount("shark")
    @DisplayName("방 삭제 - 실패(권한 없음)")
    void removeRoom_fail_forbidden() throws Exception {
        Account testUser = accountFactory.createAccount("testUser");
        Room room = roomFactory.createRoom("test-room", testUser);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/settings/room/remove")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}