package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import com.gamego.domain.room.Room;
import com.gamego.service.EventService;
import com.gamego.service.RoomService;
import com.gamego.service.query.RoomQueryService;
import com.gamego.validator.EventValidator;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/room/{path}")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RoomService roomService;
    private final RoomQueryService roomQueryService;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventView(@CurrentAccount Account account, @PathVariable String path, Model model) {

        Room room = roomQueryService.getRoomToUpdateByStatus(path, account);
        model.addAttribute("room", room);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEvent(@CurrentAccount Account account, @PathVariable String path,
                           @Valid EventForm eventForm, BindingResult bindingResult, Model model) {
        Room room = roomQueryService.getRoomToUpdateByStatus(path, account);
        if(bindingResult.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(room);
            return "event/form";
        }

        Event event = eventService.createEvent(room, account, eventForm);

        return "redirect:/room/" + room.getPath() + "/events/" + event.getId();
    }
}
