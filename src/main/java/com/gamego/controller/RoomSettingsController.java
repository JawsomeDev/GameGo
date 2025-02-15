package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomDescriptionForm;
import com.gamego.repository.GameRepository;
import com.gamego.service.RoomQueryService;
import com.gamego.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room/{path}/settings")
public class RoomSettingsController {

    private final RoomQueryService roomQueryService;
    private final RoomService roomService;
    private final ModelMapper modelMapper;
    private final GameRepository gameRepository;

    @GetMapping("/description")
    public String viewRoomSettings(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        checkAuth(account, model, room);
        model.addAttribute(room);
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(room, RoomDescriptionForm.class));
        return "room/settings/description";
    }

    @PostMapping("/description")
    public String updateRoomInfo(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid RoomDescriptionForm roomDescriptionForm, BindingResult bindingResult,
                                 Model model, RedirectAttributes attributes) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        if (bindingResult.hasErrors()) {
            model.addAttribute(account);
            checkAuth(account, model, room);
            model.addAttribute(room);
            return "room/settings/description";
        }
        roomService.updateRoomDescription(path, account, roomDescriptionForm);
        attributes.addFlashAttribute("message", "방 소개를 수정했습니다.");

        return "redirect:/room/" + room.getEncodedPath()
                 + "/settings/description";
    }

    @GetMapping("/banner")
    public String roomBannerForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        checkAuth(account, model, room);
        model.addAttribute("room", room);
        model.addAttribute(account);
        return "room/settings/banner";
    }

    @PostMapping("/banner")
    public String roomBannerUpdate(@CurrentAccount Account account, @PathVariable String path, String image
            ,RedirectAttributes attributes) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        roomService.updateRoomBanner(room, image);
        return "redirect:/room/" + room.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/disabled")
    public String disableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        roomService.disableRoomBanner(room);

        return "redirect:/room/" + room.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/enabled")
    public String enableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        roomService.enableRoomBanner(room);
        return "redirect:/room/" + room.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/default")
    public String useDefaultBanner(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        roomService.useDefaultBanner(room);
        attributes.addFlashAttribute("message", "기본 배너로 변경했습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/settings/banner";
    }

    private void checkAuth(Account account, Model model, Room room) {
        boolean isMaster = roomQueryService.isMaster(account, room);
        model.addAttribute("isMaster", isMaster);
        boolean isManagerOrMaster = roomQueryService.isManagerOrMaster(account, room);
        model.addAttribute("isManagerOrMaster", isManagerOrMaster);
    }

}
