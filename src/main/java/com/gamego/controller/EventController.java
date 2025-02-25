package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.event.form.EventUpdateForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.EventRepository;
import com.gamego.service.EventService;
import com.gamego.service.RoomService;
import com.gamego.service.query.EventQueryService;
import com.gamego.service.query.RoomQueryService;
import com.gamego.validator.EventValidator;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/room/{path}")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RoomService roomService;
    private final RoomQueryService roomQueryService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final EventQueryService eventQueryService;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventView(@CurrentAccount Account account, @PathVariable String path, Model model) {

        Room room = roomQueryService.getRoomToMakePartyByStatus(path, account);
        model.addAttribute("room", room);
        model.addAttribute(account);
        checkAuth(account, model, room);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEvent(@CurrentAccount Account account, @PathVariable String path,
                           @Valid EventForm eventForm, BindingResult bindingResult, Model model) {
        Room room = roomQueryService.getRoomToMakePartyByStatus(path, account);
        if(bindingResult.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(room);
            return "event/form";
        }

        Event event = eventService.createEvent(room, account, eventForm);

        return "redirect:/room/" + room.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEventView(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        Room room = roomQueryService.getRoom(path);
        model.addAttribute(account);
        model.addAttribute(room);
        model.addAttribute(eventQueryService.getEventWithEnrolls(id));
        checkAuth(account, model, room);
        return "event/view";
    }

    @GetMapping("/events")
    public String viewRoomEvents(@CurrentAccount Account account, @PathVariable String path,
                                 @PageableDefault(size=9, sort = "startedAt") Pageable pageable, Model model) {
        Room room = roomQueryService.getRoom(path);
        model.addAttribute(account);
        model.addAttribute(room);
        checkAuth(account, model, room);

        Page<Event> upcomingEvents = eventQueryService.getUpcomingEvents(room, pageable);
        model.addAttribute("events", upcomingEvents);


        return "room/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventView(@CurrentAccount Account account, @PathVariable String path,
                                @PathVariable Long id, Model model) {
        EventUpdateForm form = eventQueryService.getEventUpdateDto(path, id, account);
        model.addAttribute(account);
        model.addAttribute("room", form.getRoom());
        model.addAttribute("event", form.getEvent());
        model.addAttribute("eventForm", form.getEventForm());

        return "event/edit-form";

    }

    @PostMapping("/events/{id}/edit")
    public String editEvent(@CurrentAccount Account account, @PathVariable String path,
                            @PathVariable Long id, @Valid EventForm eventForm, BindingResult bindingResult, Model model) {
        Event event = eventRepository.findEnrollById(id).orElseThrow(() -> new IllegalArgumentException("해당 방이 없습니다."));
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, bindingResult);

        if(bindingResult.hasErrors()) {
            EventUpdateForm form = eventQueryService.getEventUpdateDto(path, id, account);
            model.addAttribute(account);
            model.addAttribute("room", form.getRoom());
            model.addAttribute("event", form.getEvent());
            return "event/edit-form";
        }
        eventService.updateEvent(id, eventForm);

        Room room = roomQueryService.getRoom(path);

        return "redirect:/room/" +  room.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@CurrentAccount Account account, @PathVariable String path,
                              @PathVariable Long id) {
        Room room = roomQueryService.getRoomToUpdateByStatus(path, account);
        eventService.deleteEvent(id);
        return "redirect:/room/" + room.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollView(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Long eventId) {
        Room room = roomQueryService.getRoomToEnroll(path);
        eventService.newEnroll(eventId, account);

        return "redirect:/room/" + room.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{id}/disenroll")
    public String newEnroll(@CurrentAccount Account account, @PathVariable String path,
                            @PathVariable("id") Long eventId){
        Room room = roomQueryService.getRoomToEnroll(path);
        eventService.cancelEnroll(eventId, account);

        return "redirect:/room/" + room.getEncodedPath() + "/events/" + eventId;
    }

    @GetMapping("/events/{id}/enroll/{enrollId}/accept")
    public String acceptEnroll(@CurrentAccount Account account, @PathVariable String path,
                               @PathVariable("id") Long eventId, @PathVariable("enrollId") Long enrollId) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        eventService.acceptEnroll(eventId, enrollId);
        return "redirect:/room/" + room.getEncodedPath() + "/events/" + eventId;
    }

    @GetMapping("/events/{id}/enroll/{enrollId}/reject")
    public String rejectEnroll(@CurrentAccount Account account, @PathVariable String path,
                               @PathVariable("id") Long eventId, @PathVariable("enrollId") Long enrollId) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        eventService.rejectEnroll(eventId, enrollId);
        return "redirect:/room/" + room.getEncodedPath() + "/events/" + eventId;
    }




    private void checkAuth(Account account, Model model, Room room) {
        boolean isMaster = roomQueryService.isMaster(account, room);
        model.addAttribute("isMaster", isMaster);
        boolean isManagerOrMaster = roomQueryService.isManagerOrMaster(account, room);
        model.addAttribute("isManagerOrMaster", isManagerOrMaster);
        boolean isMemberOrManager = roomQueryService.isMemberOrManager(account, room);
        model.addAttribute("isMemberOrManager", isMemberOrManager);
    }
}
