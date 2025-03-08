package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.exception.BannedMemberJoinException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;



@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentAccount")
    public Account currentAccount(@CurrentAccount Account account){
        return account;
    }

    @ExceptionHandler(BannedMemberJoinException.class)
    public String handleBannedMemberJoinException(BannedMemberJoinException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("hasMessage", false);
        return "error/join-error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            @CurrentAccount Account account,
            HttpServletRequest request,
            RuntimeException e) {
        // 로그 기록
        if(account != null){
            log.info("'{}' requested '{}'", account.getNickname(), request.getRequestURI());
        } else {
            log.info("requested '{}'", request.getRequestURI());
        }
        log.error("bad request", e);

        // 에러 페이지로 리다이렉트 (type=room-not-found)
        return "error/error";
    }


}
