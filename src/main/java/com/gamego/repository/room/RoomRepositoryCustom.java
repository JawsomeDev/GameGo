package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomRepositoryCustom {

    Page<Room> findByKeyword(String keyword, Account account, Pageable pageable);
}
