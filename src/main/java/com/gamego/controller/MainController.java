package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.EnrollRepository;
import com.gamego.repository.MessageRepository;
import com.gamego.repository.RoomAccountRepository;
import com.gamego.repository.account.AccountRepository;
import com.gamego.repository.room.RoomRepository;
import com.gamego.service.ReviewService;
import com.gamego.service.RoomService;
import com.gamego.service.query.ReviewQueryService;
import com.gamego.service.query.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;
    private final ReviewService reviewService;
    private final ReviewQueryService reviewQueryService;
    private final RoomService roomService;
    private final EnrollRepository enrollRepository;
    private final RoomQueryService roomQueryService;
    private final RoomAccountRepository roomAccountRepository;

//    @GetMapping("/")
//    public String index() {
//        return "index";
//    }

    @GetMapping("/")
    public String main(@CurrentAccount Account account, Model model) {
        if(account != null) {
            Account findAccount = accountRepository.findAccountWithGamesById(account.getId()).orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다."));
            List<Room> roomList = roomQueryService.getRoomList(findAccount);
            model.addAttribute("account", account);
            model.addAttribute("roomList", roomList);

            List<Room> allActiveRooms = roomQueryService.findActiveRooms();
            model.addAttribute("allActiveRooms", allActiveRooms);

            List<RoomAccount> roomAccountList = roomAccountRepository.findFirst9ByAccountAndRoleIn(findAccount,
                    Arrays.asList(RoomRole.MANAGER, RoomRole.MASTER));
            List<Room> masterRooms = roomAccountList.stream().map(RoomAccount::getRoom).collect(Collectors.toList());
            roomQueryService.calculateAverageRating(masterRooms);
            model.addAttribute("roomMasterOf", masterRooms);

            List<RoomAccount> roomAccountListByMember = roomAccountRepository.findFirst9ByAccountAndRole(findAccount, RoomRole.MEMBER);
            List<Room> memberRooms = roomAccountListByMember.stream().map(RoomAccount::getRoom).collect(Collectors.toList());
            roomQueryService.calculateAverageRating(memberRooms);
            model.addAttribute("roomMemberOf", memberRooms);

            return "index-after-login";
        }
        List<Room> roomList = roomQueryService.findActiveRooms();
        roomQueryService.calculateAverageRating(roomList);
        model.addAttribute("roomList", roomList);
        return "main";
    }

    @GetMapping("/search/room")
    public String searchRoom(@CurrentAccount Account account, @PageableDefault(size = 9, sort = "activeDateTime",
        direction = Sort.Direction.DESC) Pageable pageable, String keyword, Model model) {
        // freshAccount를 조회해야 account.timepreference가 출력됨.
        Account freshAccount = accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("해당 계정이 존재하지 않습니다."));
        Page<Room> roomPage = roomQueryService.getRoomWithGames(keyword, freshAccount, pageable);


        model.addAttribute("account", freshAccount);
        model.addAttribute("roomPage", roomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString()
                .contains("activeDateTime") ? "activeDateTime" : "memberCount");
        return "search";
    }
}
