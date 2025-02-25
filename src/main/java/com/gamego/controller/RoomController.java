package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.form.RoomForm;
import com.gamego.repository.RoomRepository;
import com.gamego.service.query.RoomQueryService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RoomController {


    private final RoomService roomService;
    private final RoomValidator roomValidator;
    private final RoomQueryService roomQueryService;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

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
        boolean isMemberOrManager = roomQueryService.isMemberOrManager(account, room);
        model.addAttribute("isMemberOrManager", isMemberOrManager);
    }

    @GetMapping("/room/{path}/members")
    public String viewRoomMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Room room = roomQueryService.getRoom(path);
        model.addAttribute(account);
        model.addAttribute(room);
        checkAuth(account, model, room);
        return "room/members";
    }

    @PostMapping("/room/{path}/join")
    public String joinRoom(@CurrentAccount Account account, @PathVariable String path) {
        Room room = roomService.addMember(path, account);
        return "redirect:/room/" + room.getEncodedPath() + "/members";
    }

    @PostMapping("/room/{path}/leave")
    public String leaveRoom(@CurrentAccount Account account, @PathVariable String path) {
        Room room = roomService.removeMember(path, account);
        return "redirect:/room/" + room.getEncodedPath() + "/members";
    }

    @PostMapping("/room/{path}/promote")
    public String promoteMember(@CurrentAccount Account account, @PathVariable String path,
                                @RequestParam("targetAccountId") Long targetAccountId,
                                RedirectAttributes attributes) {
        Room room = roomService.promoteMember(path, targetAccountId, account);
        attributes.addFlashAttribute("message","매니저로 승급이 완료되었습니다..");
        return "redirect:/room/" + room.getEncodedPath() + "/members";
    }

    @PostMapping("/room/{path}/demote")
    public String demoteMember(@CurrentAccount Account account, @PathVariable String path,
                               @RequestParam("targetAccountId") Long targetAccountId,
                               RedirectAttributes attributes) {
        Room room = roomService.demoteMember(path, targetAccountId, account);
        attributes.addFlashAttribute("message", "게스트로 강등이 완료되었습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/members";
    }

    @PostMapping("/room/{path}/banned")
    public String banMember(@CurrentAccount Account account, @PathVariable String path,
                            @RequestParam("targetAccountId") Long targetAccountId,
                            RedirectAttributes attributes) {
        Room room = roomService.banMember(path, targetAccountId, account);
        attributes.addFlashAttribute("message", "추방하였습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/members";
    }
}
