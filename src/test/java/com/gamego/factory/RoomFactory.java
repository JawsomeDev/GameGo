package com.gamego.factory;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.repository.room.RoomRepository;
import com.gamego.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomFactory {

    @Autowired
    RoomService roomService;

    @Autowired
    RoomRepository roomRepository;

    public Room createRoom(String path, Account account) {
        Room room = new Room();
        room.setPath(path);
        room.setTitle("test");
        room.setShortDescription("test");
        room.setLongDescription("test");
        roomService.createNewRoom(room, account);
        return room;
    }


}
