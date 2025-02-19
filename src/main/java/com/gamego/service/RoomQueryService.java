package com.gamego.service;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public Room getRoom(String path){
        Room room = roomRepository.findByPath(path);
        checkExistRoom(path, room);
        return room;
    }


    public Room getRoomToUpdate(String path, Account account) {
        Room room = this.getRoom(path);
        checkIfMaster(account, room);
        return room;
    }

    public boolean isMaster(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(ra -> ra.getAccount().getId().equals(account.getId())
                        && ra.getRole() == RoomRole.MASTER);
    }

    private static void checkIfMaster(Account account, Room room)  {
        boolean isMaster = room.getRoomAccounts().stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account)
                        && roomAccount.getRole().equals(RoomRole.MASTER));
        if (!isMaster) {
            throw new AccessDeniedException("해당 기능은 방장만 사용할 수 있습니다.");
        }
    }

    public Room getRoomToUpdateGame(String path, Account account) {
        Room room = roomRepository.findRoomWithGamesByPath(path);
        checkExistRoom(path, room);
        checkIfMaster(account, room);
        return room;
    }



    private static void checkExistRoom(String path, Room room) {
        if (room == null) {
            throw new IllegalArgumentException(path + "에 해당하는 방이 없습니다.");
        }
    }

    public String getTimePreference(Room room) {
        TimePreference timePreference = roomRepository.findById(room.getId())
                .map(Room::getTimePreference).orElse(null);
        return timePreference != null ? timePreference.getValue() : null;
    }

    public Room getRoomToUpdateByStatus(String path, Account account) {
        Room room = roomRepository.findRoomWithStatusByPath(path);
        checkIfMaster(account, room);
        checkExistRoom(path, room);
        return room;
    }

    public boolean isMemberOrManager(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(ra-> ra.getAccount().getId().equals(account.getId())
                && (ra.getRole() == RoomRole.MEMBER || ra.getRole() == RoomRole.MANAGER));
    }

    public boolean isManagerOrMaster(Account account, Room room) {
        return room.getRoomAccounts().stream()
                .anyMatch(ra -> ra.getAccount().getId().equals(account.getId())
                        && (ra.getRole() == RoomRole.MASTER || ra.getRole()== RoomRole.MANAGER));
    }
}
