package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface MessageRepository extends JpaRepository<Message, Long> {

    long countByAccountAndChecked(Account account, boolean checked);

    @Transactional
    List<Message> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean checked);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);

    void deleteByAccount(Account account);
}
