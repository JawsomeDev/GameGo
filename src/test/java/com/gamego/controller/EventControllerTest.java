package com.gamego.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.eventenum.EventType;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.factory.AccountFactory;
import com.gamego.factory.EventFactory;
import com.gamego.factory.RoomFactory;
import com.gamego.repository.EnrollRepository;
import com.gamego.repository.EventRepository;
import com.gamego.repository.account.AccountRepository;
import com.gamego.service.EventService;
import com.gamego.service.RoomService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.gamego.email.EmailService emailService;

    @Autowired
    private AccountFactory accountFactory;

    @Autowired
    EntityManager em;

    @Autowired
    private RoomFactory roomFactory;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventFactory eventFactory;
    @Autowired
    private RoomService roomService;
    @Autowired
    private EventService eventService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EnrollRepository enrollRepository;

    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 폼 조회 - 성공")
    void newEventView_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        // 'test-room'은 방 팩토리를 통해 생성
        var room = roomFactory.createRoom("test-room", shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/new-event"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 - 유효한 이벤트 폼")
    void newEvent_validForm_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        LocalDateTime endEnrolledAt = LocalDateTime.now().plusHours(1);
        LocalDateTime startedAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endedAt = startedAt.plusHours(2);

        // EventForm의 필수 필드 값 전송 (제목, 시작/종료 시간, 참가 제한, 설명)
        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/new-event")
                        .with(csrf())
                        .param("title", "Test Event")
                        .param("endEnrolledAt", endEnrolledAt.toString() )
                        .param("startedAt", startedAt.toString())
                        .param("endedAt", endedAt.toString())
                        .param("limitOfNumbers", "5")
                        .param("description", "This is a test event"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/room/*/events/*"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 - 유효하지 않은 이벤트 폼 (빈 제목, 참가 제한 2 미만)")
    void newEvent_invalidForm_failure() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        LocalDateTime endEnrolledAt = LocalDateTime.now().plusHours(1);
        LocalDateTime startedAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endedAt = startedAt.plusHours(2);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/new-event")
                        .with(csrf())
                        .param("title", "")
                        .param("endEnrolledAt", endEnrolledAt.toString())
                        .param("startedAt", startedAt.toString())
                        .param("endedAt", endedAt.toString())
                        .param("limitOfNumbers", "1")
                        .param("description", "Invalid event test"))
                // 유효성 검증에 실패하면 form 뷰가 다시 표시됨
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("event/form"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 - 유효하지 않은 이벤트 폼(등록마감일이 현재 이전)")
    void newEvent_validForm_fail_becauseOfEndEnrolledAt() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        LocalDateTime endEnrolledAt = LocalDateTime.now().minusHours(1);
        LocalDateTime startedAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endedAt = startedAt.plusHours(2);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/new-event")
                        .with(csrf())
                        .param("title", "Test Event")
                        .param("endEnrolledAt", endEnrolledAt.toString() )
                        .param("startedAt", startedAt.toString())
                        .param("endedAt", endedAt.toString())
                        .param("limitOfNumbers", "5")
                        .param("description", "This is a test event"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("event/form"));
    }


    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 - 유효하지 않은 이벤트 폼(종료일이 시작일 전)")
    void newEvent_validForm_fail_becauseOfEndedAt() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        //when
        LocalDateTime endEnrolledAt = LocalDateTime.now().plusHours(1);
        LocalDateTime startedAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endedAt = startedAt.minusHours(2);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/new-event")
                        .with(csrf())
                        .param("title", "Test Event")
                        .param("endEnrolledAt", endEnrolledAt.toString() )
                        .param("startedAt", startedAt.toString())
                        .param("endedAt", endedAt.toString())
                        .param("limitOfNumbers", "5")
                        .param("description", "This is a test event"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("event/form"));
    }



    @Test
    @WithAccount("shark")
    @DisplayName("새 이벤트 생성 - 유효하지 않은 이벤트 폼(시작일이 오늘보터 이전)")
    void newEvent_validForm_fail_becauseOfStartedAt() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);

        LocalDateTime endEnrolledAt = LocalDateTime.now().plusHours(1);
        LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime endedAt = startedAt.minusHours(2);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/new-event")
                        .with(csrf())
                        .param("title", "Test Event")
                        .param("endEnrolledAt", endEnrolledAt.toString() )
                        .param("startedAt", startedAt.toString())
                        .param("endedAt", endedAt.toString())
                        .param("limitOfNumbers", "5")
                        .param("description", "This is a test event"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("event/form"));
    }


    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 상세 페이지 조회 - 성공")
    void getEventView_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("test", room, shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/view"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 목록 조회 - 성공")
    void viewRoomEvents_success() throws Exception {

        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("test", room, shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("room/events"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 수정 폼 조회 - 성공")
    void editEventView_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("test", room, shark);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/edit-form"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("room"));

    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 수정 폼 조회 - 실패 ( 권한 x)")
    void editEventView_fail() throws Exception {
        Account test = accountFactory.createAccount("test");
        Room room = roomFactory.createRoom("test-room", test);
        Event event = eventFactory.createFCFSEvent("test", room, test);

        mockMvc.perform(get("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/edit"))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 수정 - 성공")
    void editEvent_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("test", room, shark);

        LocalDateTime newStartedAt = LocalDateTime.now().plusDays(2);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/edit")
                        .with(csrf())
                        .param("title", "Updated Event")
                        .param("description", "Updated Description")
                        .param("startedAt", newStartedAt.toString())
                        .param("endedAt", newStartedAt.plusDays(1).toString())
                        .param("endEnrolledAt", LocalDateTime.now().plusHours(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events/" + event.getId()));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 삭제 - 성공")
    void deleteEvent_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("Event To Delete", room, shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events"));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 참가 신청 - 성공")
    void newEnroll_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("Enroll Event", room, shark);


        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events/" + event.getId()));
    }

    @Test
    @WithAccount("shark")
    @DisplayName("이벤트 참가 취소 - 성공")
    void disenroll_success() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = eventFactory.createFCFSEvent("Enroll Cancel Event", room, shark);

        // 먼저 참가 신청
        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        // 참가 취소
        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events/" + event.getId()));
    }
    
    
    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소")
    @WithAccount("shark")
    void not_accetped_account_cancelEnroll() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Account test = accountFactory.createAccount("test");
        Account henzi = accountFactory.createAccount("henzi");
        
        Room room = roomFactory.createRoom("test-room", shark);
        Event event = createEvent("test-event", EventType.FCFS, 2, room, shark);
        
        eventService.newEnroll(event.getId(), test);
        eventService.newEnroll(event.getId(), henzi);
        eventService.newEnroll(event.getId(), shark);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events/" + event.getId()));
    }


    @Test
    @DisplayName("선착순 2명인데 3명이 신청했을 때 마지막은 대기자")
    @WithAccount("shark")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Account test = accountFactory.createAccount("test");
        Account henzi = accountFactory.createAccount("henzi");
        Room room = roomFactory.createRoom("test-study", shark);
        Event event = createEvent("test-event", EventType.FCFS, 2, room, shark);

        eventService.newEnroll(event.getId(), test);
        eventService.newEnroll(event.getId(), henzi);
        eventService.newEnroll(event.getId(), shark);

        assertNotNull(enrollRepository.findByEventAndAccount(event, test));
        assertNotNull(enrollRepository.findByEventAndAccount(event, henzi));
        assertNull(enrollRepository.findByEventAndAccount(event, shark));
    }


    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("shark")
    void accepted_account_cancelEnrollment_to_FCFS_event_accepted_after_cancel() throws Exception {
        Account shark = accountRepository.findByNickname("shark");
        Account test = accountFactory.createAccount("test");
        Account henzi = accountFactory.createAccount("henzi");
        Room room = roomFactory.createRoom("test-study", shark);
        Event event = createEvent("test-event", EventType.FCFS, 2, room, shark);

        eventService.newEnroll(event.getId(), test);
        eventService.newEnroll(event.getId(), shark);
        eventService.newEnroll(event.getId(), henzi);

        mockMvc.perform(post("/room/" + room.getEncodedPath() + "/events/" +event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + room.getEncodedPath() + "/events/" + event.getId()));

        assertNotNull(enrollRepository.findByEventAndAccount(event, test));
        assertNotNull(enrollRepository.findByEventAndAccount(event, henzi));
        assertNull(enrollRepository.findByEventAndAccount(event, shark));
    }


    private Event createEvent(String eventTitle, EventType eventType, int limit, Room room, Account account) {
        EventForm eventForm = new EventForm();
        eventForm.setEventType(eventType);
        eventForm.setLimitOfNumbers(limit);
        eventForm.setTitle(eventTitle);
        eventForm.setEndEnrolledAt(LocalDateTime.now().plusDays(1));
        eventForm.setStartedAt(LocalDateTime.now().plusDays(1).plusHours(5));
        eventForm.setEndedAt(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(room, account, eventForm);
    }

}
