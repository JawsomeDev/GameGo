package com.gamego.domain.game.form;


import lombok.Data;

import java.util.List;

/**
 * 게임 리스트를 화이트리스트로 저장.
 */
@Data
public class GameListResp {
    private List<String> games;
    private String whitelist;
}
