package com.gamego.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.room.QRoom;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.form.RoomForm;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.email.EmailService;
import com.gamego.exception.BannedMemberJoinException;
import com.gamego.factory.AccountFactory;
import com.gamego.factory.RoomFactory;
import com.gamego.repository.account.AccountRepository;
import com.gamego.repository.room.RoomRepository;
import com.gamego.service.RoomService;
import com.gamego.service.query.RoomQueryService;
import lombok.With;
import org.apache.coyote.BadRequestException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static com.gamego.domain.room.QRoom.room;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomQueryService roomQueryService;

    @Autowired
    private ModelMapper modelMapper;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private AccountFactory accountFactory;
    @Autowired
    private RoomFactory roomFactory;
    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("새로운 방 생성 폼 페이지 - GET /new-room")
    @WithAccount("testuser")
    void newRoomForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/new-room"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/form"))
                .andExpect(model().attributeExists("roomForm"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("새로운 방 생성 실패")
    @WithAccount("testUser")
    void createRoomForm_ShouldReturnFormView() throws Exception {
        String title = "Test Room";
        String path = "test-room";
        String shortDescription = "This is a test room";
        String longDescription = "";

        mockMvc.perform(post("/new-room")
                        .with(csrf())
                        .param("title", title)
                        .param("path", path)
                        .param("shortDescription", shortDescription)
                        .param("longDescription", longDescription))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("새로운 방 생성 성공 - POST /new-room")
    @WithAccount("testuser")
    void newRoomPost_ValidData_ShouldRedirectToRoomView() throws Exception {
        String title = "Test Room";
        String path = "test-room";
        String shortDescription = "This is a test room";
        String longDescription = "This is a test room";

        // 테스트용 Room 객체 생성 (예를 들어, getEncodedPath()는 path를 그대로 반환한다고 가정)
        Room room = new Room();
        room.setTitle(title);
        room.setPath(path);

        mockMvc.perform(post("/new-room")
                        .with(csrf())
                        .param("title", title)
                        .param("path", path)
                        .param("shortDescription", shortDescription)
                        .param("longDescription", longDescription))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/test-room"));
    }

    @Test
    @DisplayName("방 상세 페이지 조회 - GET /room/{path}")
    @WithAccount("testuser")
    void viewRoom_ShouldReturnRoomView() throws Exception {
        Room room = new Room();
        room.setTitle("Test Room");
        room.setPath("test-room");
        room.setShortDescription("test");
        room.setLongDescription("test");

        Account testuser = accountRepository.findByNickname("testuser");
        roomService.createNewRoom(room, testuser);

        mockMvc.perform(get("/room/test-room"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/view"))
                .andExpect(model().attribute("room", room))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("방 참여 성공 - POST /room/{path}/join")
    @WithAccount("testuser")
    void joinRoom_ShouldRedirectToMembers() throws Exception {
        Account henzi = accountFactory.createAccount("henzi");
        Room room = roomFactory.createRoom("test-room", henzi);

        mockMvc.perform(post("/room/" + room.getPath() + "/join")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getPath() + "/members"));


    }

    @Test
    @DisplayName("방 나가기 성공 - POST /room/{path}/leave")
    @WithAccount("testuser")
    void leaveRoom_ShouldRedirectToMembers() throws Exception {
        Account henzi = accountFactory.createAccount("henzi");
        Room room = roomFactory.createRoom("test-room", henzi);


        mockMvc.perform(post("/room/test-room/leave")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/test-room/members"));
    }

    @Test
    @DisplayName("승급 성공 - testuser가 MASTER, target은 MEMBER")
    @WithAccount("testuser")
    void promoteMember_ShouldRedirectToMembers() throws Exception {

        Account testuser = accountRepository.findByNickname("testuser");

        Room room = roomFactory.createRoom("test-room", testuser);

        Account target = accountFactory.createAccount("target");
        roomService.addMember(room.getPath(), target);


        mockMvc.perform(post("/room/" + room.getPath() + "/promote")
                        .param("targetAccountId", String.valueOf(target.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getPath() + "/members"))
                .andExpect(flash().attribute("message", "매니저로 승급이 완료되었습니다."));
    }

    @Test
    @DisplayName("승급 실패 - testuser - Member권한을 가진 사람이 승급진행")
    @WithAccount("testuser")
    void promoteMember_AccessedDeniedException() throws Exception {

        Account testuser = accountRepository.findByNickname("testuser");

        Account account = accountFactory.createAccount("henzi");

        Room room = roomFactory.createRoom("test-room", account);

        Account target = accountFactory.createAccount("target");

        roomService.addMember(room.getPath(), testuser);
        roomService.addMember(room.getPath(), target);


        mockMvc.perform(post("/room/" + room.getPath() + "/promote")
                        .param("targetAccountId", String.valueOf(target.getId()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("강등 성공 - testuser가 MASTER, target은 Manager")
    @WithAccount("testuser")
    void demoteMember_ShouldRedirectToMembers() throws Exception {

        Account testuser = accountRepository.findByNickname("testuser");

        Room room = roomFactory.createRoom("test-room", testuser);

        Account target = accountFactory.createAccount("target");
        roomService.addMember(room.getPath(), target);
        // target을 Manager로 승급
        roomService.promoteMember(room.getPath(), target.getId(), testuser);

        mockMvc.perform(post("/room/" + room.getPath() + "/demote")
                        .param("targetAccountId", String.valueOf(target.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getPath() + "/members"));
    }
    @Test
    @DisplayName("추방 성공 - testuser(MASTER)가 target(MEMBER)를 추방")
    @WithAccount("testuser")  // 시큐리티 컨텍스트에 testuser로 로그인
    void banMember_ShouldRedirectToMembers() throws Exception {

        Account testuser = accountRepository.findByNickname("testuser");


        Room room = roomFactory.createRoom("test-room", testuser);


        roomRepository.findRoomWithMemberByPath("test-room");

        Account target = accountFactory.createAccount("target");
        roomService.addMember(room.getPath(), target);

        room = roomRepository.findRoomWithMemberByPath(room.getPath());

        mockMvc.perform(post("/room/" + room.getPath() + "/banned")
                        .param("targetAccountId", String.valueOf(target.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getPath() + "/members"))
                .andExpect(flash().attribute("message", "추방하였습니다."));
    }

    @Test
    @DisplayName("추방 후에 재가입 실패해야함")
    @WithAccount("testuser")
    void cannotAddMemberAfterBannedRoom() throws Exception {
        Account testuser = accountRepository.findByNickname("testuser");

        Room room = roomFactory.createRoom("test-room", testuser);

        Account target = accountFactory.createAccount("target");
        roomService.addMember(room.getPath(), target);

        // when
        roomService.banMember(room.getPath(), target.getId(), testuser);


        Assertions.assertThatThrownBy(() -> roomService.addMember(room.getPath(), target))
                .isInstanceOf(BannedMemberJoinException.class)
                .hasMessageContaining("추방된 회원은 재가입 할 수 없습니다.");
    }
}
