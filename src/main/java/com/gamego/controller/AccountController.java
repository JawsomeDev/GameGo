package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.dto.AccountForm;
import com.gamego.domain.account.dto.PasswordForm;
import com.gamego.repository.AccountRepository;
import com.gamego.service.AccountQueryService;
import com.gamego.service.AccountService;
import com.gamego.validator.AccountValidator;
import com.gamego.validator.PasswordValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountValidator accountValidator;
    private final AccountRepository accountRepository;
    private final AccountQueryService accountQueryService;
    private final PasswordValidator passwordValidator;

    @InitBinder("passwordForm")
    public void initPasswordBinder(WebDataBinder binder) {
        binder.addValidators(passwordValidator);
    }

    @InitBinder("accountForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(accountValidator);
    }

    @GetMapping("/login")
    public String login(@CurrentAccount Account account) {
        if(account != null){
            return "redirect:/main";
        }
        return "account/login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("accountForm", new AccountForm());
        return "account/signup";
    }

    @PostMapping("/sign-up")
    public String signupSubmit(@Valid @ModelAttribute("accountReq") AccountForm accountForm,
                               BindingResult bindingResult,Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("accountForm", accountForm);
            return "account/signup";
        }

        accountService.createAccount(accountForm);

        return "redirect:/login";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if(account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }
        accountService.completeSignUp(account);
        model.addAttribute("numberOfUsers", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

   @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute(account.getEmail());
        return "account/check-email";
   }

   @GetMapping("/resend-checked-email")
    public String resendEmail(@CurrentAccount Account account, Model model) {
        if(!account.canSendEmailAgain()){
            model.addAttribute("account", account);
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        model.addAttribute("account", account);
        accountService.sendVerificationEmail(account);
        return "redirect:/main";
   }

   @GetMapping("/profile/{nickname}")
    public String profile(@PathVariable String nickname, Model model, @CurrentAccount Account account) {
       Account myAccount = accountQueryService.getAccount(nickname);
       boolean isOwner = account.getNickname().equals(myAccount.getNickname());

       model.addAttribute("currentAccount", account);
       model.addAttribute("account", myAccount);
       model.addAttribute("isOwner", isOwner);

       return "account/profile";
   }

   @GetMapping("/reset-password")
    public String resetPasswordForm(String token, Model model) {
        model.addAttribute("token", token);
        return "account/reset-password";
   }

   @PostMapping("/reset-password")
    public String processResetPasswordRequest(@RequestParam String email, Model model) {
       Account account = accountRepository.findByEmail(email);
       if(account == null){
           model.addAttribute("error", "유효하지 않은 이메일입니다.");
           return "account/reset-password";
       }
       if(!account.canSendPasswordResetEmail()){
           model.addAttribute("error2", "비밀번호 찾기 링크는 1시간에 한 번만 전송할 수 있습니다.");
           return "account/reset-password";
       }

       accountService.sendResetPasswordEmail(account);
       model.addAttribute("message", "입력하신 이메일로 비밀번호 재설정 링크를 전송했습니다.");
       return "account/reset-password";
   }

   @GetMapping("/reset-password/confirm")
    public String resetPasswordConfirmForm(String token, Model model) {
       Account account = accountRepository.findByResetPasswordToken(token);
       if(account == null || !account.isResetPasswordTokenValid(token)){
           model.addAttribute("error", "유효하지 않거나 만료된 링크입니다.");
           return "account/reset-password-confirm";
       }
       model.addAttribute("token", token);
       model.addAttribute("passwordForm", new PasswordForm());
       return "account/reset-password-confirm";
   }

   @PostMapping("/reset-password/confirm")
    public String processResetPasswordConfirm(@RequestParam("token") String token,
                                              @Valid PasswordForm passwordForm,
                                              BindingResult bindingResult,
                                              Model model,
                                              RedirectAttributes attributes) {
       Account account = accountRepository.findByResetPasswordToken(token);
       if(bindingResult.hasErrors()) {
           return "account/reset-password-confirm";
       }

        if(account ==null ||!account.isResetPasswordTokenValid(token)){
            model.addAttribute("error", "유효하지 않거나 만료된 토큰입니다.");
            return "account/reset-password-confirm";
        }

       accountService.updatePassword(account, passwordForm.getNewPassword());
       attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
       return "redirect:/login";
   }
}
