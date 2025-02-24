package com.gamego.repository;

import com.gamego.domain.event.Event;
import com.gamego.domain.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"enrolls", "createBy"})
    Optional<Event> findEnrollById(Long id);

    @EntityGraph(attributePaths = {"enrolls"})
    Page<Event> findByRoomAndStartedAtAfterOrderByStartedAt(Room room, LocalDateTime startedAtAfter, Pageable pageable);
}
