package com.gamego.domain.room.event;

import com.gamego.domain.room.Room;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class RoomBannedEvent {

    private final Room room;
    private final Long bannedAccountId;
    private final String message;
}
