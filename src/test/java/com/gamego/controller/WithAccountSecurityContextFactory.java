package com.gamego.controller;

import com.gamego.domain.account.dto.AccountForm;
import com.gamego.repository.AccountRepository;
import com.gamego.service.AccountService;
import com.gamego.service.CustomUserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    private final CustomUserService customUserService;

    public WithAccountSecurityContextFactory(AccountService accountService, AccountRepository accountRepository, CustomUserService customUserService) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.customUserService = customUserService;
    }

    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        String nickname = annotation.value();

        AccountForm accountForm = new AccountForm();
        accountForm.setNickname(nickname);
        accountForm.setEmail(nickname + "@gmail.com");
        accountForm.setPassword("12341234");
        accountForm.setConfirmPassword("12341234");
        accountService.createAccount(accountForm);
        accountRepository.flush();

        UserDetails principal = customUserService.loadUserByUsername(nickname);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, accountForm.getPassword(), authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
