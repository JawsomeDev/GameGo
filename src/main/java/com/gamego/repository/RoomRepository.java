package com.gamego.repository;

import com.gamego.domain.room.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"games", "roomAccounts.account"})
    Room findByPath(String path);

    @EntityGraph(attributePaths = {"games", "roomAccounts.account"})
    Room findRoomWithGamesByPath(String path);

    @EntityGraph(attributePaths = {"games"})
    Room findRoomWithGamesById(Long id);
}
