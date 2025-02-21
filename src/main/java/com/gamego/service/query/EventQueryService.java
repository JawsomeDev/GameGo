package com.gamego.service.query;


import com.gamego.domain.event.Event;
import com.gamego.domain.room.Room;
import com.gamego.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventQueryService {

    private final EventRepository eventRepository;

    public EventQueryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event getEventWithEnrolls(Long id){
        return eventRepository.findEnrollById(id).orElseThrow(() -> new IllegalArgumentException("해당 모집이 없습니다."));
    }

    public List<Event> getUpcomingEvents(Room room) {
        return eventRepository.findByRoomAndStartedAtAfterOrderByStartedAt(room, LocalDateTime.now());
    }
}
