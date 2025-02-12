package com.gamego.domain.game.dto;


import com.gamego.domain.game.Game;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameReq {


    @NotNull
    private String gameName;


    public Game getGame(){
        return Game.builder()
                .name(gameName)
                .build();
    }
}
