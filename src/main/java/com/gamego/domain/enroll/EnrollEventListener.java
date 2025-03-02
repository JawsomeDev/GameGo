package com.gamego.domain.enroll;


import com.gamego.config.AppProperties;
import com.gamego.domain.account.Account;
import com.gamego.domain.event.Event;
import com.gamego.domain.messages.Message;
import com.gamego.domain.messages.MessageType;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.email.EmailMessage;
import com.gamego.email.EmailService;
import com.gamego.repository.MessageRepository;
import com.gamego.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Async
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnrollEventListener {

    private final AppProperties appProperties;
    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;

    @EventListener
    public void handleEnrollEvent(EnrollEvent enrollEvent) {
        Enroll enroll = enrollEvent.getEnroll();
        Account account = enroll.getAccount();
        Event event = enroll.getEvent();
        Room room = roomRepository.findRoomWithRoomAccountsById(enroll.getEvent().getRoom().getId());


        Set<Account> accounts = room.getRoomAccounts().stream()
                .map(RoomAccount::getAccount).collect(Collectors.toSet());

        accounts.forEach( a -> {
            if(a.isGameEnrollmentResultByEmail()) {
                sendEmail(enrollEvent, a, event, room);
                log.info("Enroll event has been processed by Email");
            }

            if(a.isGameEnrollmentResultByWeb()){
                sendMessage(enrollEvent, a, event, room);
                log.info("Enroll event has been processed by Web");
            }
        });



    }

    private void sendMessage(EnrollEvent enrollEvent, Account account, Event event, Room room) {
        Message message = new Message();
        message.setTitle(room.getTitle() + " / " + event.getTitle());
        message.setLink("/room/" + room.getEncodedPath() + "/events/" + event.getId());
        message.setChecked(false);
        message.setCreatedDateTime(LocalDateTime.now());
        message.setMessage(enrollEvent.getMessage());
        message.setAccount(account);
        message.setMessageType(MessageType.ENROLLMENT);
        messageRepository.save(message);
    }

    private void sendEmail(EnrollEvent enrollEvent, Account account, Event event, Room room) {
        Context context = new Context();
        context.setVariable("link", "/room/" + room.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", room.getTitle());
        context.setVariable("message", enrollEvent.getMessage());
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .from("hyuk2000s@gmail.com")
                .to(account.getEmail())
                .subject("겜할래, " + event.getTitle() + " 파티 참가 신청 결과입니다.")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
