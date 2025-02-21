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

    public Map<String, List<Event>> classifyEventsByTime(Room room){
        List<Event> events = eventRepository.findByRoomOrderByStartedAt(room);

        Map<Boolean, List<Event>> partitioned = events.stream()
                .collect(Collectors.partitioningBy(e -> !e.getEndedAt().isBefore(LocalDateTime.now())));

        Map<String, List<Event>> result = new HashMap<>();
        result.put("newEvents", partitioned.get(true));
        result.put("oldEvents", partitioned.get(false));
        return result;
    }
}
