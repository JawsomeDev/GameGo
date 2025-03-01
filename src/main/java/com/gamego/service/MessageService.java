package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.messages.Message;
import com.gamego.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {


    private final MessageRepository messageRepository;

    public void markAsRead(List<Message> messages) {
        messages.forEach(message -> message.changeChecked(true));
        messageRepository.saveAll(messages);
    }

    public void deleteMessage(Account account) {
        messageRepository.deleteByAccountAndChecked(account, true);
    }
}
