package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollRepository extends JpaRepository<Enroll, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enroll findByEventAndAccount(Event event, Account account);

    @EntityGraph(attributePaths = {"events", "event.room"})
    List<Enroll> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, boolean accepted);
}
