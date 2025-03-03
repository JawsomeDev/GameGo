package com.gamego.repository;

import com.gamego.domain.account.Account;
import com.gamego.domain.review.Review;
import com.gamego.domain.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"account"})
    Page<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    Optional<Review> findByRoomAndAccount(Room room, Account account);

    List<Review> findReviewsByRoom(Room room);
}
