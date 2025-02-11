package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Transactional(readOnly = true)
public interface RoomAccountRepository extends JpaRepository<RoomAccount, Long> {

    Optional<RoomAccount> findByRoomAndAccount(Room room, Account account);
}
