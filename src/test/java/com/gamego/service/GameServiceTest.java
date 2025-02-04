package com.gamego.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Test
    @DisplayName("게임 목록 보기")
    void getSteamGame() {
        List<String> top1000Games = gameService.getTop1000Games();
        System.out.println(top1000Games.size());
        for (String top100Game : top1000Games) {
            System.out.println(top100Game);
        }
    }
}