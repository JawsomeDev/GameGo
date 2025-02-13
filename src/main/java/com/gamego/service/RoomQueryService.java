package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.game.dto.GameTagResp;
import com.gamego.domain.room.dto.RoomDescriptionReq;
import com.gamego.domain.room.dto.RoomMemberResp;
import com.gamego.domain.room.dto.RoomResp;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
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
        roomResp.setManager(isManager(account, room));
        roomResp.setMaster(isMaster(account, room));
        roomResp.setUser(isMember(account, room));
        roomResp.setGames(mapGames(room));
        roomResp.setMembers(mapRoomMembers(room));


        if (room.getTimePreference() != null) {
            roomResp.setTimePreference(room.getTimePreference().getValue());
        } else {
            roomResp.setTimePreference(null);
        }

        return roomResp;
    }

    public RoomResp getRoomToUpdate(String path, Account account) {
        RoomResp roomResp = this.getRoom(path, account);
        checkIfMaster(roomResp);
        return roomResp;
    }

    private List<RoomMemberResp> mapRoomMembers(Room room) {
        return room.getRoomAccounts().stream()
                .map(roomAccount -> {
                    RoomMemberResp roomMemberResp = new RoomMemberResp();
                    roomMemberResp.setNickname(roomAccount.getAccount().getNickname());
                    roomMemberResp.setProfileImage(roomAccount.getAccount().getProfileImage());
                    roomMemberResp.setRole(roomAccount.getRole().name());
                    return roomMemberResp;
                })
                .collect(Collectors.toList());
    }

    private List<GameTagResp> mapGames(Room room) {
        return room.getGames().stream()
                .map(game -> modelMapper.map(game, GameTagResp.class))
                .collect(Collectors.toList());
    }

    private boolean isMember(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account)
                && roomAccount.getRole().equals(RoomRole.MEMBER));
    }

    private static boolean isManagerOrMaster(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account)
                        && (roomAccount.getRole() == RoomRole.MANAGER || roomAccount.getRole() == RoomRole.MASTER));
    }

    private boolean isManager(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(roomAccount ->  roomAccount.getAccount().equals(account)
                && roomAccount.getRole() == RoomRole.MANAGER);
    }

    private boolean isMaster(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(roomAccount ->  roomAccount.getAccount().equals(account)
                && roomAccount.getRole() == RoomRole.MASTER);
    }

    private static int getMemberCount(Room room) {
        return room.getRoomAccounts().size();
    }

    private static void checkIfMaster(RoomResp roomResp)  {
        if(!roomResp.isMaster()){
            throw new AccessDeniedException("해당 기능은 방장만 사용 가능합니다.");
        }
    }
}
