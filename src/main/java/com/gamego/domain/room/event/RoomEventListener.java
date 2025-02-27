package com.gamego.domain.room.event;


import com.gamego.config.AppProperties;
import com.gamego.domain.account.Account;
import com.gamego.domain.messages.Message;
import com.gamego.domain.messages.MessageType;
import com.gamego.domain.room.Room;
import com.gamego.email.EmailMessage;
import com.gamego.email.EmailService;
import com.gamego.repository.account.AccountRepository;
import com.gamego.repository.MessageRepository;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class RoomEventListener {

    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final MessageRepository messageRepository;

    @EventListener
    public void handleRoomCreatedEvent(RoomCreatedEvent roomCreatedEvent) {
        Room room = roomRepository.findRoomWithGamesAndTimePreferenceById(roomCreatedEvent.getRoom().getId());

        List<Account> accounts = accountRepository.findByGamesAndTimes(room);

        accounts.forEach(account -> {
            if(account.isGameCreatedByEmail()){
                Context context = new Context();
                context.setVariable("link", "/room/" + room.getEncodedPath());
                context.setVariable("nickname", account.getNickname());
                context.setVariable("linkName", room.getTitle());
                context.setVariable("message", "새로운 방이 생겼습니다.");
                context.setVariable("host", appProperties.getHost());

                String message = templateEngine.process("mail/simple-link", context);

                EmailMessage emailMessage = EmailMessage.builder()
                        .from("hyuk2000s@gmail.com")
                        .to(account.getEmail())
                        .subject("겜할래, '" + room.getTitle() + "' 방이 생겼습니다.")
                        .message(message)
                        .build();

                emailService.sendEmail(emailMessage);
            }
            if(account.isGameCreatedByWeb()){
                Message message = new Message();
                message.setTitle(room.getTitle());
                message.setLink("/room/" + room.getEncodedPath());
                message.setChecked(false);
                message.setCreatedDateTime(LocalDateTime.now());
                message.setMessage(room.getShortDescription());
                message.setAccount(account);
                message.setMessageType(MessageType.CREATED);
                messageRepository.save(message);
            }
        });
    }
}


