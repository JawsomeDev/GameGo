package com.gamego.repository;

import com.gamego.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {


    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);
    Optional<Account> findByNickname(String nickname);

    // 이메일 또는 닉네임으로 찾기
    Optional<Account> findByEmailOrNickname(String email, String nickname);
}
