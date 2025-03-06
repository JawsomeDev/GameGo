package com.gamego.factory;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.AccountRole;
import com.gamego.repository.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@gmail.com");
        account.setAccountRole(AccountRole.USER);
        accountRepository.save(account);
        return account;
    }
}
