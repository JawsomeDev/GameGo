package com.gamego.domain.account.accountenum;


import com.gamego.domain.game.Game;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameForm {


    @NotNull
    private String gameName;


    public Game getGame(){
        return Game.builder()
                .name(gameName)
                .build();
    }
}
