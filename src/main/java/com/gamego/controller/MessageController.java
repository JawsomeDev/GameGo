package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.messages.Message;
import com.gamego.repository.MessageRepository;
import com.gamego.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final MessageService messageService;

    @GetMapping("/messages")
    public String getMessages(@CurrentAccount Account account, Model model) {
        List<Message> messages = messageRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = messageRepository.countByAccountAndChecked(account, true);
        putCategorizedMessages(messages, numberOfChecked, messages.size(), model);
        model.addAttribute(account);
        model.addAttribute("isNew", true);
        messageService.markAsRead(messages);
        return "message/list";
    }

    @GetMapping("/messages/old")
    public String getOldMessages(@CurrentAccount Account account, Model model) {
        List<Message> messages = messageRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = messageRepository.countByAccountAndChecked(account, false);
        putCategorizedMessages(messages, messages.size(), numberOfNotChecked, model);
        model.addAttribute(account);
        model.addAttribute("isNew", false);
        return "message/list";
    }

    @PostMapping("/messages/delete")
    public String deleteMessages(@CurrentAccount Account account) {
        messageRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/messages";
    }

    private void putCategorizedMessages(List<Message> messages, long numberOfChecked,
                                        long numberOfNotChecked, Model model) {
        List<Message> newRoomMessages = new ArrayList<>();
        List<Message> eventEnrollMessages = new ArrayList<>();
        List<Message> updateRoomMessages = new ArrayList<>();
        for (var message : messages) {
            switch (message.getMessageType()){
                case CREATED : newRoomMessages.add(message); break;
                case ENROLLMENT : eventEnrollMessages.add(message); break;
                case UPDATED : updateRoomMessages.add(message); break;
            }
        }
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("messages", messages);
        model.addAttribute("newRoomMessages", newRoomMessages);
        model.addAttribute("eventEnrollMessages", eventEnrollMessages);
        model.addAttribute("updateRoomMessages", updateRoomMessages);
    }
}
