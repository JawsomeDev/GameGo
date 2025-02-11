package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomAccountRepository;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {


    private final RoomRepository roomRepository;
    private final RoomAccountRepository roomAccountRepository;

    public Room createNewRoom(Room room, Account account) {
        Room newRoom = roomRepository.save(room);

        RoomAccount roomAccount = new RoomAccount(newRoom, account, RoomRole.MASTER, LocalDateTime.now());
        roomAccountRepository.save(roomAccount);

        return newRoom;
    }

    public Room getRoom(String path) {
        Room room = roomRepository.findByPath(path);
        if(room == null) {
            throw new IllegalArgumentException(path + "에 해당하는 방이 없습니다.");
        }
        return room;
    }
}
