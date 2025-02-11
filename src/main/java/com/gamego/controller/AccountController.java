package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.accountenum.SignUpForm;
import com.gamego.repository.AccountRepository;
import com.gamego.service.AccountService;
import com.gamego.validator.SignUpFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(signUpFormValidator);
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
       Account myAccount = accountService.getAccount(nickname);

       model.addAttribute("account", myAccount);
       model.addAttribute("isOwner", myAccount.equals(account));

       return "account/profile";
   }
}
