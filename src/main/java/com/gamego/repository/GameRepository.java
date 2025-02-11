package com.gamego.repository;

import com.gamego.domain.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional(readOnly = true)
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findByName(String name);

    boolean existsByName(String name);

   List<Game> findAllByNameIn(Collection<String> names);
}
