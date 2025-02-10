package com.gamego.repository;

import com.gamego.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByName(String name);

    boolean existsByName(String name);

   List<Game> findAllByNameIn(Collection<String> names);
}
