package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.dto.RoomReq;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.service.RoomQueryService;
import com.gamego.service.RoomService;
import com.gamego.validator.RoomValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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


    private final RoomService roomService;
    private final RoomValidator roomValidator;
    private final RoomQueryService roomQueryService;

    @InitBinder("roomReq")
    public void roomFormInitBinder(WebDataBinder binder) {
        binder.addValidators(roomValidator);
    }


    @GetMapping("/new-room")
    public String newRoomForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new RoomReq());
        return "room/form";
    }

    @PostMapping("/new-room")
    public String newRoomUpdate(@CurrentAccount Account account, @Valid RoomReq roomReq,
                                BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute(account);
            return "room/form";
        }
        RoomResp newRoom = roomService.createNewRoom(roomReq, account);
        return "redirect:/room/" + URLEncoder.encode(newRoom.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/room/{path}")
    public String viewRoom(@CurrentAccount Account account, @PathVariable String path, Model model) {
        RoomResp roomResp = roomQueryService.getRoom(path);
        model.addAttribute(account);
        model.addAttribute("room", roomResp);
        return "room/view";
    }
}
