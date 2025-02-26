package com.gamego.domain.room.event;


import com.gamego.domain.room.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Async
@Component
@Transactional(readOnly = true)
public class RoomEventListener {

    @EventListener
    public void handleRoomCreatedEvent(RoomCreatedEvent roomCreatedEvent) {
        Room room = roomCreatedEvent.getRoom();
        log.info(room.getTitle() + "is created");
        // TODO 이메일 보내거나, DB에 저장

    }
}


