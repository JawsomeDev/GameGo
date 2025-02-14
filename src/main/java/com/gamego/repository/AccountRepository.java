package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {


    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);

    @EntityGraph(attributePaths = {"games"})
    Optional<Account> findAccountWithGamesById(Long id);
}
