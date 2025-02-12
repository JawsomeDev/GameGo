package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomReq;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomAccountRepository;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    public RoomResp createNewRoom(RoomReq roomReq, Account account) {
        Room room = modelMapper.map(roomReq, Room.class);
        Room savedRoom = roomRepository.save(room);

        RoomAccount roomAccount = new RoomAccount(savedRoom, account, RoomRole.MASTER, LocalDateTime.now());
        roomAccountRepository.save(roomAccount);

        return modelMapper.map(savedRoom, RoomResp.class);
    }

}
