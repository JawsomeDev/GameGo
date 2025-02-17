package com.gamego.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.account.dto.TimePreferenceForm;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.dto.GameForm;
import com.gamego.domain.game.dto.GameResp;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomDescriptionForm;
import com.gamego.repository.GameRepository;
import com.gamego.service.RoomQueryService;
import com.gamego.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room/{path}/settings")
public class RoomSettingsController {

    private final RoomQueryService roomQueryService;
    private final RoomService roomService;
    private final ModelMapper modelMapper;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

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

    @GetMapping("/games")
    public String updateGames(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Room room = roomQueryService.getRoomToUpdateGame(path, account);
        checkAuth(account, model, room);
        model.addAttribute(account);
        model.addAttribute(room);
        model.addAttribute("games", room.getGames().stream().map(Game :: toString).collect(Collectors.toList()));

        List<String> allGames = gameRepository.findAll().stream().map(Game :: toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allGames));

        return "room/settings/games";
    }

    @PostMapping("/games/add")
    @ResponseBody
    public ResponseEntity<?> addGame(@CurrentAccount Account account, @PathVariable String path,
                                     @RequestBody GameForm gameForm) {
        Room room = roomQueryService.getRoomToUpdateGame(path, account);
        Game game = gameRepository.findByName(gameForm.getGameName());

        if (game == null) {
            return ResponseEntity.badRequest()
                    .body("{\"status\":\"error\",\"message\":\"등록된 게임만 선택 가능합니다.\"}");
        }

        GameResp response = roomService.addGame(room, game);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/games/remove")
    @ResponseBody
    public ResponseEntity<?> removeGame(@CurrentAccount Account account, @PathVariable String path,
                                        @RequestBody GameForm gameForm) {
        Room room = roomQueryService.getRoomToUpdateGame(path, account);
        Game game = gameRepository.findByName(gameForm.getGameName());
        if (game == null) {
            return ResponseEntity.badRequest().build();
        }

        GameResp response = roomService.removeGame(room, game);
        return ResponseEntity.ok(response);
    }

    private void checkAuth(Account account, Model model, Room room) {
        boolean isMaster = roomQueryService.isMaster(account, room);
        model.addAttribute("isMaster", isMaster);
        boolean isManagerOrMaster = roomQueryService.isManagerOrMaster(account, room);
        model.addAttribute("isManagerOrMaster", isManagerOrMaster);
    }

    @GetMapping("/times")
    public String updateTimes(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        model.addAttribute(account);
        model.addAttribute(room);

        String timePreference = roomQueryService.getTimePreference(room);
        model.addAttribute("timePreference", timePreference);

        List<String> whitelist = Stream.of(TimePreference.values())
                .map(TimePreference :: getValue).toList();

        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));

        return "room/settings/times";
    }

    @PostMapping("/times/add")
    @ResponseBody
    public ResponseEntity<?> addTimePreference(@CurrentAccount Account account,@PathVariable String path,
                                               @RequestBody TimePreferenceForm form) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        String selectedTime = form.getTimePreference();
        TimePreference timePreference = TimePreference.fromValue(selectedTime);

        roomService.addTimePreference(room, timePreference);
        Map<String, String> response = new HashMap<>();
        response.put("status", "add");
        response.put("timePreference", selectedTime);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/times/remove")
    @ResponseBody
    public ResponseEntity<?> removeTimePreference(@CurrentAccount Account account, @PathVariable String path,
                                                  @RequestBody TimePreferenceForm form) {
        Room room = roomQueryService.getRoomToUpdate(path, account);
        roomService.removeTimePreference(room);
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");
        return ResponseEntity.ok(response);
    }



}
