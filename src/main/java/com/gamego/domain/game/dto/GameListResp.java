package com.gamego.domain.game.dto;


import lombok.Data;

import java.util.List;

@Data
public class GameListResp {

    private List<String> games;
    private String whitelist;
}
