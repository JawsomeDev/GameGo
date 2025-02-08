package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.form.Messages;
import com.gamego.domain.account.form.PasswordForm;
import com.gamego.domain.account.form.ProfileForm;
import com.gamego.service.AccountService;
import com.gamego.validator.PasswordFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Controller
public class ProfileController {


    private final ModelMapper modelMapper;
    private final AccountService accountService;


    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(new PasswordFormValidator());
    }

    @GetMapping("/settings/profile")
    public String profileToUpdate(@CurrentAccount Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute("profileForm", modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateAccount(@CurrentAccount Account account, @Valid @ModelAttribute ProfileForm profileForm,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/profile";
        }
        modelMapper.map(profileForm, account);
        accountService.updateAccount(account, profileForm);
        redirectAttributes.addFlashAttribute("message", "프로필을 수정했습니다.");
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
        model.addAttribute("messages", modelMapper.map(account, Messages.class));
        return "settings/messages";
    }

    @PostMapping("/settings/messages")
    public String updateMessages(@CurrentAccount Account account, @Valid Messages messages, BindingResult bindingResult,
                                 Model model, RedirectAttributes attributes){
        if(bindingResult.hasErrors()){
            model.addAttribute("account", account);
            return "settings/messages";
        }

        accountService.updateMessages(account, messages);
        attributes.addFlashAttribute("error", "메시지 설정을 변경했습니다.");
        return "redirect:/settings/messages";
    }
}
