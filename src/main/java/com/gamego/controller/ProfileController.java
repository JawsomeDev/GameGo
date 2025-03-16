package com.gamego.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.account.accountenum.*;
import com.gamego.domain.account.form.*;
import com.gamego.domain.game.Game;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.game.form.GameListResp;
import com.gamego.domain.game.form.GameForm;
import com.gamego.domain.game.form.GameResp;
import com.gamego.repository.GameRepository;
import com.gamego.repository.account.AccountRepository;
import com.gamego.service.GameService;
import com.gamego.service.query.AccountQueryService;
import com.gamego.service.AccountService;
import com.gamego.validator.NicknameValidator;
import com.gamego.validator.PasswordValidator;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Controller
public class ProfileController {

    private final NicknameValidator nicknameValidator;
    private final ModelMapper modelMapper;
    private final AccountService accountService;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final AccountQueryService accountQueryService;
    private final EntityManager em;
    private final GameService gameService;
    private final AccountRepository accountRepository;

    @InitBinder("nicknameForm")
    public void initBinder1(WebDataBinder binder){
        binder.addValidators(nicknameValidator);
    }

    @InitBinder("passwordForm")
    public void initBinder2(WebDataBinder binder) {
        binder.addValidators(new PasswordValidator());
    }

    @GetMapping("/settings/profile")
    public String profileToUpdate(@CurrentAccount Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute("profileForm", modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateAccount(@CurrentAccount Account account, @Valid @ModelAttribute ProfileForm profileForm,
                                BindingResult bindingResult, Model model, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/profile";
        }

        accountService.updateAccount(account, profileForm);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/password")
    public String passwordToUpdate(@CurrentAccount Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm,
                                 BindingResult bindingResult, Model model, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/password";
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/settings/messages")
    public String messageToUpdate(@CurrentAccount Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute("messageForm", modelMapper.map(account, MessageForm.class));
        return "settings/messages";
    }

    @PostMapping("/settings/messages")
    public String updateMessages(@CurrentAccount Account account, @Valid MessageForm messageForm, BindingResult bindingResult,
                                 Model model, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/messages";
        }

        accountService.updateMessages(account, messageForm);
        attributes.addFlashAttribute("error", "메시지 설정을 변경했습니다.");
        return "redirect:/settings/messages";
    }

    @GetMapping("/settings/games")
    public String gameToSelect(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        GameListResp response = accountQueryService.getGameListResponse(account);
        model.addAttribute("games", response.getGames());
        model.addAttribute("whitelist", response.getWhitelist());
        return "settings/games";
    }

    @PostMapping("/settings/games/add")
    @ResponseBody
    public ResponseEntity<?> addGame(@CurrentAccount Account account,
                                     @RequestBody @Valid GameForm gameForm) {
        Game game = gameService.findOrCreateNew(gameForm.getGameName());
        GameResp response = accountService.addGame(account, game);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/settings/games/remove")
    @ResponseBody
    public ResponseEntity<?> removeGame(@CurrentAccount Account account,
                                        @RequestBody @Valid GameForm gameForm) {
        Game game = gameRepository.findByName(gameForm.getGameName());
        if (game == null) {
            return ResponseEntity.badRequest().build();
        }

        GameResp response = accountService.removeGame(account, game);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings/times")
    public String timeToSelect(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute("account", account);

        String timePreference = accountQueryService.getTimePreference(account);

        model.addAttribute("timePreference", timePreference);

        List<String> whitelist = Stream.of(TimePreference.values())
                .map(TimePreference :: getValue).toList();

        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));

        return "settings/times";
    }

    @ResponseBody
    @PostMapping("/settings/times/add")
    public ResponseEntity<?> addTimePreference(@CurrentAccount Account account, @RequestBody TimePreferenceForm form){

        String selectedPreference = form.getTimePreference();
        TimePreference timePreference = TimePreference.fromValue(selectedPreference);

        accountService.addTimePreference(account, timePreference);
        Map<String, String> response = new HashMap<>();
        response.put("status", "add");
        response.put("timePreference", selectedPreference);

        return ResponseEntity.ok(response);
    }


    @ResponseBody
    @PostMapping("/settings/times/remove")
    public ResponseEntity<?> removeTimePreference(@CurrentAccount Account account){
        accountService.removeTimePreference(account);
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings/account")
    public String accountToUpdate(@CurrentAccount Account account, Model model){
        Account updatedAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        model.addAttribute("account", updatedAccount);
        model.addAttribute(modelMapper.map(updatedAccount, NicknameForm.class));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm,
                                BindingResult bindingResult, Model model, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/account";
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        Account updatedAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        AccountUserDetails updatedDetails = new AccountUserDetails(updatedAccount);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(updatedDetails, updatedDetails.getPassword(), updatedDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/settings/account";
    }

    @PostMapping("/settings/account/delete")
    public String deleteAccount(@CurrentAccount Account account, HttpServletRequest request, HttpServletResponse response){
        accountService.deleteAccount(account);


        request.getSession().invalidate();

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return "redirect:/";
    }
}
