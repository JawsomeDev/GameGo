package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.form.SignUpForm;
import com.gamego.repository.AccountRepository;
import com.gamego.service.AccountService;
import com.gamego.validator.SignUpFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(signUpFormValidator);
    }

    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/signup";
    }

    @PostMapping("/sign-up")
    public String signupSubmit(@Valid @ModelAttribute("signUpForm") SignUpForm signUpForm,
                               BindingResult bindingResult,Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("signUpForm", signUpForm);
            return "account/signup";
        }

        accountService.createAccount(signUpForm);

        return "redirect:/login";
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

}
