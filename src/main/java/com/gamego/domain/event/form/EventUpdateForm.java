package com.gamego.domain.event.form;


import com.gamego.domain.event.Event;
import com.gamego.domain.room.Room;
import lombok.Data;

@Data
public class EventUpdateForm {

    private final Room room;
    private final Event event;
    private final EventForm eventForm;
}
