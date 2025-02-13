package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.dto.GameTagResp;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public RoomResp getRoom(String path, Account account){
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException(path + "에 해당하는 방이 없습니다.");
        }

        RoomResp roomResp = modelMapper.map(room, RoomResp.class);

        roomResp.setMemberCount(getMemberCount(room));
        roomResp.setJoinAble(room.isJoinable(account));
        roomResp.setManagerOrMaster(isManagerOrMaster(account, room));
        roomResp.setGames(mapGames(room));

        if (room.getTimePreference() != null) {
            roomResp.setTimePreference(room.getTimePreference().getValue());
        } else {
            roomResp.setTimePreference(null);
        }

        return roomResp;
    }

    private List<GameTagResp> mapGames(Room room) {
        return room.getGames().stream()
                .map(game -> modelMapper.map(game, GameTagResp.class))
                .collect(Collectors.toList());
    }

    private static boolean isManagerOrMaster(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account)
                        && (roomAccount.getRole() == RoomRole.MANAGER || roomAccount.getRole() == RoomRole.MASTER));
    }

    private static int getMemberCount(Room room) {
        return room.getRoomAccounts().size();
    }
}
