package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.enroll.EnrollAcceptedEvent;
import com.gamego.domain.enroll.EnrollRejectedEvent;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.event.RoomUpdateEvent;
import com.gamego.repository.EnrollRepository;
import com.gamego.repository.EventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;
    private final EnrollRepository enrollRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(new RoomUpdateEvent(event.getRoom(), "파티가 생성되었습니다."));
        return eventRepository.save(event);

    }


    public void updateEvent(Long id, @Valid EventForm eventForm) {
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

    public void acceptEnroll(Long eventId, Long enrollId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("파티가 없습니다."));
        Enroll enroll = enrollRepository.findById(enrollId).orElseThrow(() -> new IllegalArgumentException("잘못된 등록입니다."));
        event.accept(enroll);
        eventPublisher.publishEvent(new EnrollAcceptedEvent(enroll));
    }

    public void rejectEnroll(Long eventId, Long enrollId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("파티가 없습니다."));
        Enroll enroll = enrollRepository.findById(enrollId).orElseThrow(() -> new IllegalArgumentException("잘못된 등록입니다."));
        event.reject(enroll);
        eventPublisher.publishEvent(new EnrollRejectedEvent(enroll));
    }

    public void checkInEnroll(Long enrollId) {
        Enroll enroll = enrollRepository.findById(enrollId).orElseThrow(() -> new IllegalArgumentException("잘못된 등록입니다."));
        enroll.checkIn(enroll);
    }

    public void cancelCheckInEnroll(Long enrollId) {
        Enroll enroll = enrollRepository.findById(enrollId).orElseThrow(() -> new IllegalArgumentException("잘못된 등록입니다."));
        enroll.cancelCheckIn(enroll);
    }
}
