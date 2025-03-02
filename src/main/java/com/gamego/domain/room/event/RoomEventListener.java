package com.gamego.domain.room.event;


import com.gamego.config.AppProperties;
import com.gamego.domain.account.Account;
import com.gamego.domain.messages.Message;
import com.gamego.domain.messages.MessageType;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.email.EmailMessage;
import com.gamego.email.EmailService;
import com.gamego.repository.account.AccountRepository;
import com.gamego.repository.MessageRepository;
import com.gamego.repository.room.RoomRepository;
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
import java.util.Set;
import java.util.stream.Collectors;


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
                sendEmail(account, room,
                        "새로운 방이 생겼습니다.", "겜할래, '" + room.getTitle() + "' 방이 생겼습니다.");
            }
            if(account.isGameCreatedByWeb()){
                sendMessage(account, room,  MessageType.CREATED, roomCreatedEvent.getMessage());
            }
        });
    }

    @EventListener
    public void handleRoomUpdateEvent(RoomUpdateEvent roomUpdateEvent){
        Room room = roomRepository.findRoomWithMemberById(roomUpdateEvent.getRoom().getId());
        Set<Account> accounts = room.getRoomAccounts().stream()
                .map(RoomAccount::getAccount).collect(Collectors.toSet());

        accounts.forEach(account -> {
            if(account.isGameUpdatedByEmail()){
                sendEmail(account, room, "새로운 파티를 모집중입니다." ,
                        "참가하신 방 " + room.getTitle() + "에 새로운 파티를 모집중입니다."
                );
            }
            if(account.isGameUpdatedByWeb()){
                sendMessage(account, room, MessageType.UPDATED, roomUpdateEvent.getMessage());
            }
        });
    }

    @EventListener
    public void handleRoomBannedEvent(RoomBannedEvent roomBannedEvent) {
        // 벤 대상자의 계정을 직접 조회합니다.
        Account bannedAccount = accountRepository.findById(roomBannedEvent.getBannedAccountId())
                .orElseThrow(() -> new IllegalArgumentException("벤 대상 계정을 찾을 수 없습니다."));

        // 방 정보는 필요하다면 조회 (예: 메시지에 방 제목을 포함하기 위함)
        Room room = roomRepository.findRoomWithMemberById(roomBannedEvent.getRoom().getId());

        // 벤 대상자에게 메시지 전송
        sendMessage(bannedAccount, room, MessageType.BANNED, roomBannedEvent.getMessage());
    }

    private void sendMessage(Account account, Room room, MessageType messageType, String contextMessage) {
        Message message = new Message();
        message.setTitle(room.getTitle());
        message.setLink("/room/" + room.getEncodedPath());
        message.setChecked(false);
        message.setCreatedDateTime(LocalDateTime.now());
        message.setMessage(contextMessage);
        message.setAccount(account);
        message.setMessageType(messageType);
        messageRepository.save(message);
    }

    private void sendEmail(Account account, Room room, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("link", "/room/" + room.getEncodedPath());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", room.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .from("hyuk2000s@gmail.com")
                .to(account.getEmail())
                .subject(emailSubject)
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}


