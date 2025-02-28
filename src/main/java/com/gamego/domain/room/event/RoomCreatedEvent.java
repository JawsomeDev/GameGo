package com.gamego.domain.room.event;

import com.gamego.domain.room.Room;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class RoomCreatedEvent {

    private final Room room;
}
