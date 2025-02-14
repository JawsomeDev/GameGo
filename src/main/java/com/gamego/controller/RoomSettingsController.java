package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.dto.RoomDescriptionReq;
import com.gamego.domain.room.dto.RoomResp;
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

    @GetMapping("/description")
    public String viewRoomSettings(@CurrentAccount Account account, @PathVariable String path, Model model) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);

        RoomDescriptionReq roomDescriptionReq = modelMapper.map(roomResp, RoomDescriptionReq.class);

        model.addAttribute(account);
        model.addAttribute("roomDescriptionReq", roomDescriptionReq);
        model.addAttribute("room", roomResp);
        return "room/settings/description";
    }

    @PostMapping("/description")
    public String updateRoomInfo(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid RoomDescriptionReq roomDescriptionReq, BindingResult bindingResult,
                                 Model model, RedirectAttributes attributes) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        if (bindingResult.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute("room", roomResp);
            return "room/settings/description";
        }

        roomService.updateRoomDescription(path, account, roomDescriptionReq);
        attributes.addFlashAttribute("message", "방 소개를 수정했습니다.");

        return "redirect:/room/" + URLEncoder.encode(roomResp.getPath(), StandardCharsets.UTF_8)
                 + "/settings/description";
    }

    @GetMapping("/banner")
    public String roomBannerForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        model.addAttribute("room", roomResp);
        model.addAttribute(account);
        return "room/settings/banner";
    }

    @PostMapping("/banner")
    public String roomBannerUpdate(@CurrentAccount Account account, @PathVariable String path, String image
            ,RedirectAttributes attributes) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        roomService.updateRoomBanner(roomResp, image);
        return "redirect:/room/" + URLEncoder.encode(roomResp.getPath(), StandardCharsets.UTF_8) + "/settings/banner";
    }

    @PostMapping("/banner/disabled")
    public String disableBanner(@CurrentAccount Account account, @PathVariable String path) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        roomService.disableRoomBanner(roomResp);

        return "redirect:/room/" + URLEncoder.encode(roomResp.getPath(), StandardCharsets.UTF_8) + "/settings/banner";
    }

    @PostMapping("/banner/enabled")
    public String enableBanner(@CurrentAccount Account account, @PathVariable String path) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        roomService.enableRoomBanner(roomResp);
        return "redirect:/room/" + URLEncoder.encode(roomResp.getPath(), StandardCharsets.UTF_8) + "/settings/banner";
    }

    @PostMapping("/banner/default")
    public String useDefaultBanner(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        RoomResp roomResp = roomQueryService.getRoomToUpdate(path, account);
        roomService.useDefaultBanner(roomResp);
        attributes.addFlashAttribute("message", "기본 배너로 변경했습니다.");
        return "redirect:/room/" + URLEncoder.encode(roomResp.getPath(), StandardCharsets.UTF_8) + "/settings/banner";
    }

}
