package com.gamego.domain.account.form;

import lombok.Data;

@Data
public class MessageForm {

    private boolean gameCreatedByEmail;

    private boolean gameCreatedByWeb;

    private boolean gameEnrollmentResultByEmail;

    private boolean gameEnrollmentResultByWeb;

    private boolean gameUpdatedByEmail;

    private boolean gameUpdatedByWeb;

}
