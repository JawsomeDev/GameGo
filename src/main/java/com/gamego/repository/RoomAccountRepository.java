package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.roomaccount.RoomRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Transactional(readOnly = true)
public interface RoomAccountRepository extends JpaRepository<RoomAccount, Long> {

    @EntityGraph(attributePaths = {"room"})
    List<RoomAccount> findFirst9ByAccountAndRole(Account account, RoomRole role);

    @EntityGraph(attributePaths = {"room"})
    List<RoomAccount> findFirst9ByAccountAndRoleIn(Account account, List<RoomRole> roles);
}
