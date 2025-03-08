package com.gamego.factory;


import com.gamego.domain.account.Account;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.eventenum.EventType;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.EventRepository;
import com.gamego.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@Slf4j
public class EventFactory {

    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;

    public EventFactory(EventService eventService, ModelMapper modelMapper, EventRepository eventRepository) {
        this.eventService = eventService;
        this.modelMapper = modelMapper;
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Room room, Account account, EventForm eventForm) {
        Event event = Event.builder()
                .createBy(account)
                .room(room)
                .createdAt(LocalDateTime.now())
                .title(eventForm.getTitle())
                .description(eventForm.getDescription())
                .startedAt(eventForm.getStartedAt())
                .endEnrolledAt(eventForm.getEndEnrolledAt())
                .endedAt(eventForm.getEndedAt())
                .limitOfNumbers(eventForm.getLimitOfNumbers())
                .eventType(eventForm.getEventType())
                .enrolls(new ArrayList<>())
                .build();


        // 이벤트 생성 후, RoomUpdateEvent 이벤트 게시
//        eventPublisher.publishEvent(new RoomUpdateEvent(event.getRoom(), "파티가 생성되었습니다."));
        return eventRepository.save(event);
    }


    public Event createFCFSEvent(String title, Room room, Account account) {
        EventForm eventForm = new EventForm();
        eventForm.setTitle(title);
        eventForm.setDescription("Default event description");
        eventForm.setStartedAt(LocalDateTime.now().plusDays(1));
        eventForm.setEndedAt(LocalDateTime.now().plusDays(1).plusHours(2));
        eventForm.setEndEnrolledAt(LocalDateTime.now().plusHours(12));
        eventForm.setLimitOfNumbers(5);
        eventForm.setEventType(EventType.FCFS);
        return createEvent(room, account, eventForm);
    }


}
