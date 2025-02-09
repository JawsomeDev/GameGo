package com.gamego.domain.account.form;


import com.gamego.domain.Game;
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
