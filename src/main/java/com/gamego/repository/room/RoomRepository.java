package com.gamego.repository.room;

import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.room.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"games", "roomAccounts.account"})
    Room findByPath(String path);

    @EntityGraph(attributePaths = {"roomAccounts", "roomAccounts.account"})
    Room findRoomWithMemberByPath(String path);

    @EntityGraph(attributePaths = {"games", "roomAccounts.account"})
    Room findRoomWithGamesByPath(String path);

    @EntityGraph(attributePaths = {"games"})
    Room findRoomWithGamesById(Long id);

    @EntityGraph(attributePaths = {"roomAccounts.account"})
    Room findRoomWithStatusByPath(String path);

    @EntityGraph(attributePaths = {"path"})
    Room findRoomOnlyByPath(String path);

    @EntityGraph(attributePaths = {"games"})
    Room findRoomWithGamesAndTimePreferenceById(Long id);

    @EntityGraph(attributePaths = {"roomAccounts", "roomAccounts.account"})
    Room findRoomWithMemberById(Long id);

    @EntityGraph(attributePaths = {"roomAccounts"})
    Room findRoomWithRoomAccountsById(Long id);

    @EntityGraph(attributePaths = {"games", "reviews"})
    List<Room> findFirst9ByActiveAndClosedOrderByActiveDateTimeDesc(boolean active, boolean closed);
}
