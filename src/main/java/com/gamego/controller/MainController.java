package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.room.Room;
import com.gamego.repository.MessageRepository;
import com.gamego.repository.account.AccountRepository;
import com.gamego.repository.room.RoomRepository;
import com.gamego.service.ReviewService;
import com.gamego.service.RoomService;
import com.gamego.service.query.ReviewQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/main")
    public String main(@CurrentAccount Account account, Model model) {
        if(account != null) {
            Account findAccount = accountRepository.findAccountWithGamesById(account.getId()).orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다."));
            model.addAttribute("account", account);
//            model.addAttribute("enroll")
        }
        return "main";
    }

    @GetMapping("/search/room")
    public String searchRoom(@CurrentAccount Account account, @PageableDefault(size = 9, sort = "activeDateTime",
        direction = Sort.Direction.DESC) Pageable pageable, String keyword, Model model) {
        // freshAccount를 조회해야 account.timepreference가 출력됨.
        Account freshAccount = accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("해당 계정이 존재하지 않습니다."));
        Page<Room> roomPage = roomRepository.findByKeyword(keyword, freshAccount, pageable);
        for (Room room : roomPage) {
            Double averageRating = reviewQueryService.getAverageRating(room);
            roomService.saveReviewScore(averageRating, room);
        }
        model.addAttribute("account", freshAccount);
        model.addAttribute("roomPage", roomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString()
                .contains("activeDateTime") ? "activeDateTime" : "memberCount");
        return "search";
    }
}
