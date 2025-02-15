package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomForm;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.service.RoomQueryService;
import com.gamego.service.RoomService;
import com.gamego.validator.RoomValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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
    private final ModelMapper modelMapper;

    @InitBinder("roomForm")
    public void roomFormInitBinder(WebDataBinder binder) {
        binder.addValidators(roomValidator);
    }


    @GetMapping("/new-room")
    public String newRoomForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new RoomForm());
        return "room/form";
    }

    @PostMapping("/new-room")
    public String newRoomUpdate(@CurrentAccount Account account, @Valid RoomForm roomForm,
                                BindingResult bindingResult, Model model, ModelMap modelMap) {
        if(bindingResult.hasErrors()) {
            model.addAttribute(account);
            return "room/form";
        }
        Room newRoom = roomService.createNewRoom(modelMapper.map(roomForm, Room.class), account);
        return "redirect:/room/" + newRoom.getEncodedPath();
    }

    @GetMapping("/room/{path}")
    public String viewRoom(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomQueryService.getRoom(path);
        model.addAttribute(account);
        checkAuth(account, model, room);
        model.addAttribute(room);
        return "room/view";
    }

    private void checkAuth(Account account, Model model, Room room) {
        boolean isMaster = roomQueryService.isMaster(account, room);
        model.addAttribute("isMaster", isMaster);
        boolean isManagerOrMaster = roomQueryService.isManagerOrMaster(account, room);
        model.addAttribute("isManagerOrMaster", isManagerOrMaster);
    }

    @GetMapping("/room/{path}/members")
    public String viewRoomMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomQueryService.getRoom(path);
        model.addAttribute(account);
        checkAuth(account, model, room);
        model.addAttribute(room);
        return "room/members";
    }
}
