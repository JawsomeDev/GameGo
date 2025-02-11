package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.form.RoomForm;
import com.gamego.repository.RoomRepository;
import com.gamego.service.RoomService;
import com.gamego.validator.RoomFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final ModelMapper modelMapper;
    private final RoomFormValidator roomFormValidator;

    @InitBinder("roomForm")
    public void roomFormInitBinder(WebDataBinder binder) {
        binder.addValidators(roomFormValidator);
    }


    @GetMapping("/new-room")
    public String newRoomForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new RoomForm());
        return "room/form";
    }

    @PostMapping("/new-room")
    public String newRoomUpdate(@CurrentAccount Account account, @Valid RoomForm roomForm,
                                BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute(account);
            return "room/form";
        }
        Room newRoom = roomService.createNewRoom(modelMapper.map(roomForm, Room.class), account);
        return "redirect:/room/" + URLEncoder.encode(newRoom.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/room/{path}")
    public String viewRoom(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomService.getRoom(path);

        model.addAttribute(account);
        model.addAttribute(room);
        return "room/view";
    }
}
