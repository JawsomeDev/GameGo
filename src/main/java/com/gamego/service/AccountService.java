package com.gamego.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.config.AppProperties;
import com.gamego.domain.account.dto.*;
import com.gamego.domain.game.Game;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.dto.GameListResp;
import com.gamego.domain.game.dto.GameResp;
import com.gamego.email.EmailMessage;
import com.gamego.email.EmailService;
import com.gamego.repository.AccountRepository;
import com.gamego.repository.GameRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AppProperties appProperties;
    private final SpringTemplateEngine templateEngine;
    private final EmailService emailService;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/sign-up")
    public String signup() {
        return "signup";
    }

    // 회원가입
    public void createAccount(@Valid AccountReq accountReq) {

        accountReq.setPassword(passwordEncoder.encode(accountReq.getPassword()));

        Account account = modelMapper.map(accountReq, Account.class);
        accountRepository.save(account);

        sendVerificationEmail(account);
    }

    /*
        이메일 전송은 좀 시간이 걸릴 수도있으니 비동기로 처리해봄.
     */

    public void sendVerificationEmail(Account account) {

        account.generateEmailCheckToken();
        accountRepository.save(account);


        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "겜할래 서비스의 프로필 수정 기능을 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .from("hyuk2000s@gmail.com")
                .to(account.getEmail())
                .message(message)
                .subject("겜할래, 이메일 인증")
                .build();
        emailService.sendEmail(emailMessage);
    }

    /*
        이메일 인증 성공
     */
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    private static void login(Account account) {
        AccountUserDetails userDetails = new AccountUserDetails(account);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, account.getPassword(),userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public AccountResp getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if(account == null) {
            throw new IllegalArgumentException(account + "에 해당하는 사용자가 없습니다.");
        }
        return modelMapper.map(account, AccountResp.class);
    }

    public void updateAccount(Account account, @Valid ProfileReq profileReq) {
        modelMapper.map(profileReq, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, @Length(min = 8, max = 50) String newPassword) {
        account.changePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateMessages(Account account, @Valid MessageReq messageReq) {
        modelMapper.map(messageReq, account);
        accountRepository.save(account);
    }

    public GameListResp getGameListResponse(Account account) throws JsonProcessingException {
        Account accountWithGames = accountRepository.findAccountWithGamesById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        List<String> gameNames = accountWithGames.getGames()
                .stream()
                .map(Game::getName)
                .collect(Collectors.toList());

        List<String> allGames = gameRepository.findAll()
                .stream()
                .map(Game::getName)
                .collect(Collectors.toList());

        GameListResp response = new GameListResp();
        response.setGames(gameNames);
        response.setWhitelist(objectMapper.writeValueAsString(allGames));
        return response;
    }

    public GameResp addGame(Account account, Game game) {
        Account accountWithGames = accountRepository.findAccountWithGamesById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        accountWithGames.getGames().add(game);

        GameResp response = new GameResp();
        response.setStatus("add");
        response.setGameTitle(game.getName());
        return response;
    }

    public GameResp removeGame(Account account, Game game) {
        Account accountWithGames = accountRepository.findAccountWithGamesById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        accountWithGames.getGames().remove(game);

        GameResp response = new GameResp();
        response.setStatus("remove");
        response.setGameTitle(game.getName());
        return response;
    }


    public String getTimePreference(Account account){
        TimePreference timePreference = accountRepository.findById(account.getId())
                .map(Account::getTimePreference).orElse(null);
       return timePreference != null ? timePreference.getValue() : null;
    }

    public void addTimePreference(Account account, TimePreference timePreference) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> {
            a.updateTimePreference(timePreference);
        });
    }

    public void removeTimePreference(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> {
            a.updateTimePreference(null);
        });
    }

    public void updateNickname(Account account, String nickname) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> {
            a.changeNickname(nickname);
        });
    }

    public void deleteAccount(Account account) {
        accountRepository.delete(account);
    }
}
