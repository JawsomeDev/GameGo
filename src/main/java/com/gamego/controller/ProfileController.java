package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.account.form.ProfileForm;
import com.gamego.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@RequiredArgsConstructor
@Controller
public class ProfileController {


    private final ModelMapper modelMapper;
    private final AccountService accountService;

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
}
