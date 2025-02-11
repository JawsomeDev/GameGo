package com.gamego.repository;

import com.gamego.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RoomRepository extends JpaRepository<Room, Long> {

}
