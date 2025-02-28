    package com.gamego.domain.room.event;


    import com.gamego.domain.room.Room;
    import lombok.Getter;
    import lombok.RequiredArgsConstructor;

    @Getter
    @RequiredArgsConstructor
    public class RoomUpdateEvent {

        private final Room room;

        private final String message;

    }
