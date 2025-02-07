package com.gamego.service;


import com.gamego.config.AppProperties;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.account.form.ProfileForm;
import com.gamego.domain.account.form.SignUpForm;
import com.gamego.email.EmailMessage;
import com.gamego.email.EmailService;
import com.gamego.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

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


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/sign-up")
    public String signup() {
        return "signup";
    }


    public void createAccount(@Valid SignUpForm signUpForm) {

        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        Account account = modelMapper.map(signUpForm, Account.class);

        account.generateEmailCheckToken();
        accountRepository.save(account);
        sendVerificationEmail(account);
    }

    /*
        이메일 전송은 좀 시간이 걸릴 수도있으니 비동기로 처리해봄.
     */
    @Async
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

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if(account == null) {
            throw new IllegalArgumentException(account + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

    public void updateAccount(Account account, @Valid ProfileForm profileForm) {
        accountRepository.save(account);
    }

    public void updatePassword(Account account, @Length(min = 8, max = 50) String newPassword) {
        account.changePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
