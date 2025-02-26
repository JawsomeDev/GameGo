package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByAccountAndCheckedOrderByCreatedDateTime(Account account, boolean checked);

    long countByAccountAndChecked(Account account, boolean checked);

    void deleteByAccountAndChecked(Account account, boolean checked);
}
