package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomDescriptionReq;
import com.gamego.domain.room.dto.RoomReq;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomAccountRepository;
import com.gamego.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public void updateRoomDescription(String path, Account account, @Valid RoomDescriptionReq roomDescriptionReq) {
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        modelMapper.map(roomDescriptionReq, room);
    }

    public void updateRoomBanner(RoomResp roomResp, String image) {
        Room room = roomRepository.findByPath(roomResp.getPath());
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        room.updateBanner(image);
    }

    public void disableRoomBanner(RoomResp roomResp) {
        Room room = roomRepository.findByPath(roomResp.getPath());
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        room.disableBanner();
    }

    public void enableRoomBanner(RoomResp roomResp) {
        Room room = roomRepository.findByPath(roomResp.getPath());
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        room.enableBanner();
    }

    public void useDefaultBanner(RoomResp roomResp) {
        Room room = roomRepository.findByPath(roomResp.getPath());
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        room.defaultImage();

    }
}
