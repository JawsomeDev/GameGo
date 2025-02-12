package com.gamego.domain.account.dto;

import lombok.Data;

@Data
public class MessageReq {

    private boolean gameCreatedByEmail;

    private boolean gameCreatedByWeb;

    private boolean gameEnrollmentResultByEmail;

    private boolean gameEnrollmentResultByWeb;

    private boolean gameUpdatedByEmail;

    private boolean gameUpdatedByWeb;

}
