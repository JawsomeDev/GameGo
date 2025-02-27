package com.gamego.service;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.event.Event;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.form.GameResp;
import com.gamego.domain.room.Room;
import com.gamego.domain.room.event.RoomCreatedEvent;
import com.gamego.domain.room.form.RoomDescriptionForm;
import com.gamego.domain.roomaccount.BanHistory;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import com.gamego.exception.BannedMemberJoinException;
import com.gamego.repository.BanHistoryRepository;
import com.gamego.repository.EventRepository;
import com.gamego.repository.RoomAccountRepository;
import com.gamego.repository.RoomRepository;
import com.gamego.service.query.RoomQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {


    private final RoomRepository roomRepository;
    private final RoomAccountRepository roomAccountRepository;
    private final ModelMapper modelMapper;
    private final RoomQueryService roomQueryService;
    private final EventRepository eventRepository;
    private final BanHistoryRepository banHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Room createNewRoom(Room room, Account account) {
        Room savedRoom = roomRepository.save(room);
        RoomAccount roomAccount = new RoomAccount(savedRoom, account.getNickname(), account, RoomRole.MASTER, LocalDateTime.now());
        roomAccountRepository.save(roomAccount);

        return savedRoom;
    }


    public void updateRoomDescription(String path, Account account, @Valid RoomDescriptionForm roomDescriptionForm) {
        Room room = roomRepository.findByPath(path);
        if (room == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        modelMapper.map(roomDescriptionForm, room);
    }

    public void updateRoomBanner(Room room, String image) {
        Room findRoom = roomRepository.findByPath(room.getPath());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.updateBanner(image);
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

    public GameResp addGame(Room room, Game game) {
        Room findRoom = roomRepository.findRoomWithGamesById(room.getId());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.getGames().add(game);

        GameResp response = new GameResp();
        response.setStatus("add");
        response.setGameTitle(game.getName());
        return response;
    }

    public GameResp removeGame(Room room, Game game) {
        Room findRoom = roomRepository.findRoomWithGamesById(room.getId());
        if (findRoom == null) {
            throw new IllegalArgumentException("방을 찾을 수 없습니다.");
        }
        findRoom.getGames().remove(game);

        GameResp response = new GameResp();
        response.setStatus("remove");
        response.setGameTitle(game.getName());
        return response;
    }

    public void addTimePreference(Room room, TimePreference timePreference) {
        Optional<Room> byId = roomRepository.findById(room.getId());
        byId.ifPresent(r ->
                r.updateTimePreference(timePreference));
    }

    public void removeTimePreference(Room room) {
        Optional<Room> byId = roomRepository.findById(room.getId());
        byId.ifPresent(r -> r.updateTimePreference(null));
    }

    public void active(Room room) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        findRoom.active();
        eventPublisher.publishEvent(new RoomCreatedEvent(findRoom));
    }

    public void close(Room room) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        findRoom.close();
    }

    public void startRecruit(Room room) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        findRoom.changeRecruiting(true);
        // 시간 횟수 업데이트 로직
        findRoom.updateRecruitmentChangeTracking();
    }

    public void stopRecruit(Room room) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        findRoom.changeRecruiting(false);

        findRoom.updateRecruitmentChangeTracking();
    }

    public boolean isValidPath(String newPath) {
        if(!newPath.matches("^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")){
            return false;
        }
        return !roomRepository.existsByPath(newPath);
    }

    public void updateRoomPath(Room room, String newPath) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        findRoom.changePath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateRoomTitle(Room room, String newTitle) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        findRoom.changeTitle(newTitle);
    }

    public void removeRoom(Room room) {
        Room findRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        List<Event> events = eventRepository.findByRoom(findRoom);
        eventRepository.deleteAll(events);
        roomRepository.delete(findRoom);
    }

    public Room addMember(String path, Account account) {
        Room findRoom = roomRepository.findRoomWithMemberByPath(path);
        boolean isBanned = banHistoryRepository.existsByRoomIdAndAccountId(findRoom.getId(), account.getId());
        if(isBanned){
            throw new BannedMemberJoinException("추방된 회원은 재가입할 수 없습니다.");
        }
        findRoom.addMember(account);
        return findRoom;
    }

    public Room removeMember(String path, Account account) {
        Room findRoom = roomRepository.findRoomWithMemberByPath(path);
        findRoom.removeMember(account);
        return findRoom;
    }

    public Room promoteMember(String path, Long targetAccountId, Account account) {
        Room room = roomRepository.findRoomWithMemberByPath(path);
        if(!roomQueryService.isMaster(account, room)){
            throw new AccessDeniedException("권한이 없습니다.");
        }
        RoomAccount targetRoomAccount = room.getRoomAccounts().stream()
                .filter(ra -> ra.getAccount().getId().equals(targetAccountId) && ra.getRole() == RoomRole.MEMBER)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다."));
        targetRoomAccount.promoteMember();
        return room;
    }

    public Room demoteMember(String path, Long targetAccountId, Account account) {
        Room room = roomRepository.findRoomWithMemberByPath(path);
        if(!roomQueryService.isMaster(account, room)){
            throw new AccessDeniedException("권한이 없습니다.");
        }
        RoomAccount targetRoomAccount = room.getRoomAccounts().stream()
                .filter(ra -> ra.getAccount().getId().equals(targetAccountId) && ra.getRole() == RoomRole.MANAGER)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다."));
        targetRoomAccount.demoteMember();
        return room;
    }

    public Room banMember(String path, Long targetAccountId, Account account) {
        Room room = roomRepository.findRoomWithMemberByPath(path);
        if(!roomQueryService.isMaster(account, room)){
            throw new AccessDeniedException("권한이 없습니다.");
        }


        RoomAccount targetRoomAccount = room.getRoomAccounts().stream()
                .filter(ra -> ra.getAccount().getId().equals(targetAccountId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다."));



        room.getRoomAccounts().remove(targetRoomAccount);
        room.bannedMember(account);
        roomAccountRepository.delete(targetRoomAccount);

        List<Event> events = eventRepository.findAllByRoomId(room.getId());
        for (Event event : events) {
            event.getEnrolls().removeIf(enroll -> enroll.getAccount().getId().equals(targetAccountId));
            eventRepository.save(event);
        }
        BanHistory banHistory = BanHistory.builder()
                .roomId(room.getId())
                .accountId(targetAccountId)
                .bannedAt(LocalDateTime.now())
                .build();
        banHistoryRepository.save(banHistory);
        return room;
    }
}
