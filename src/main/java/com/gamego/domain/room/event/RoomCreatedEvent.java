package com.gamego.domain.room.event;

import com.gamego.domain.room.Room;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoomCreatedEvent {

    private Room room;

    public RoomCreatedEvent(Room room) {
        this.room = room;
    }
}
