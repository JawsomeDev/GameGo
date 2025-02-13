package com.gamego.domain.game.dto;


import lombok.Data;


/**
 * 게임 태그를 Room 뷰에 전달하기위한 DTO
 */
@Data
public class GameTagResp {

    private Long id;

    private String title;
}
