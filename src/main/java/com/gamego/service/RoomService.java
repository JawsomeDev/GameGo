package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.RoomDescriptionForm;
import com.gamego.domain.room.dto.RoomForm;
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
    private final GameService gameService;

    public Room createNewRoom(Room room, Account account) {
        Room savedRoom = roomRepository.save(room);
        RoomAccount roomAccount = new RoomAccount(savedRoom, account, RoomRole.MASTER, LocalDateTime.now());
        roomAccountRepository.save(roomAccount);

        return savedRoom;
    }


    public void updateRoomDescription(String path, Account account, @Valid RoomDescriptionForm roomDescriptionForm) {
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        modelMapper.map(room, room);
    }

    public void updateRoomBanner(Room room, String image) {
        Room findRoom = roomRepository.findByPath(room.getPath());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        room.updateBanner(image);
    }

    public void disableRoomBanner(Room room) {
        Room findRoom = roomRepository.findByPath(room.getPath());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.disableBanner();
    }

    public void enableRoomBanner(Room room) {
        Room findRoom = roomRepository.findByPath(room.getPath());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.enableBanner();
    }

    public void useDefaultBanner(Room room) {
        Room findRoom = roomRepository.findByPath(room.getPath());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.defaultImage();

    }

}
