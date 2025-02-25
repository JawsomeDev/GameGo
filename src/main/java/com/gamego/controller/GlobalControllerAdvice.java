package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.exception.BannedMemberJoinException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentAccount")
    public Account currentAccount(@CurrentAccount Account account){
        return account;
    }

    @ExceptionHandler(BannedMemberJoinException.class)
    public String handleBannedMemberJoinException(BannedMemberJoinException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/join-error";
    }


}
