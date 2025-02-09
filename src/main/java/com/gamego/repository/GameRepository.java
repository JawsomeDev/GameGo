package com.gamego.repository;

import com.gamego.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByName(String name);

    boolean existsByName(String name);
}
