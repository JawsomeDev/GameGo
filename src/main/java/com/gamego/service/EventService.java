package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.EnrollRepository;
import com.gamego.repository.EventRepository;
import com.gamego.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final EnrollRepository enrollRepository;

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
                .build();
        return eventRepository.save(event);
    }


    public void updateEvent(String path, Long id, Account account, @Valid EventForm eventForm) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));

        modelMapper.map(eventForm, event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        eventRepository.delete(event);
    }

    public void newEnroll(Long eventId, Account account) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("해당 모집이 없습니다."));
        boolean alreadyEnrolled = enrollRepository.existsByEventAndAccount(event, account);
        if (alreadyEnrolled) {
            throw new IllegalStateException("이미 신청된 사용자 입니다.");
        }
        Enroll enroll = Enroll.builder()
                .enrolledAt(LocalDateTime.now())
                .accepted(event.isAbleToAcceptWaitingEnroll())
                .account(account)
                .build();
        event.addEnroll(enroll);
    }

    public void cancelEnroll(Long eventId, Account account) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("해당 모집이 없습니다."));
        Enroll enroll = enrollRepository.findByEventAndAccount(event, account);
        if(!enroll.isAttended()) {
            event.removeEnroll(enroll);
            enrollRepository.delete(enroll);
            event.acceptNextWaitingEnroll();
        }
    }
}
