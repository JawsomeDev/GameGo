package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RoomRepositoryCustom {

    Page<Room> findByKeyword(String keyword, Account account, Pageable pageable);


    List<Room> findByAccount(Set<Game> games, TimePreference timePreference);

//    Map<Long, Double> findAverageRatingForRooms(List<Long> roomIds);
}
