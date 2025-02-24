package com.gamego.service.query;


import com.gamego.domain.account.Account;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.event.form.EventUpdateForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.EventRepository;
import com.gamego.service.RoomService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventQueryService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final RoomService roomService;
    private final RoomQueryService roomQueryService;

    public EventQueryService(EventRepository eventRepository, ModelMapper modelMapper, RoomService roomService, RoomQueryService roomQueryService) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.roomService = roomService;
        this.roomQueryService = roomQueryService;
    }

    public Event getEventWithEnrolls(Long id){
        return eventRepository.findEnrollById(id).orElseThrow(() -> new IllegalArgumentException("해당 모집이 없습니다."));
    }

    public Page<Event> getUpcomingEvents(Room room, Pageable pageable) {
        return eventRepository.findByRoomAndStartedAtAfterOrderByStartedAt(room, LocalDateTime.now(), pageable);
    }

    public EventUpdateForm getEventUpdateDto(String path, Long eventId, Account account){
        Room room = roomQueryService.getRoomToUpdate(path, account);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모집이 없습니다."));

        EventForm eventForm = modelMapper.map(event, EventForm.class);

        return new EventUpdateForm(room, event, eventForm);
    }
}
