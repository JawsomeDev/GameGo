package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;

    public Event createEvent(Room room, Account account, EventForm eventForm) {
        Event event = Event.builder()
                .createBy(account)
                .room(room)
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
}
