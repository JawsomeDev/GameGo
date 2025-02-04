package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.form.SignUpForm;
import com.gamego.service.AccountService;
import com.gamego.validator.SignUpFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SignUpFormValidator signUpFormValidator;

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

}
