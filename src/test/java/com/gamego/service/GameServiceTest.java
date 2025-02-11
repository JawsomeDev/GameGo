package com.gamego.service;


import com.gamego.domain.game.Game;
import com.gamego.repository.GameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GameServiceTest {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameRepository gameRepository;

    @Test
    @DisplayName("게임 목록 보기")
    void getSteamGame() {
        List<Game> all = gameRepository.findAll();
        for (Game game : all) {
            System.out.println(game);
        }
    }
}