package com.gamego.domain.account.form;

import lombok.Data;

@Data
public class Messages {

    private boolean gameCreatedByEmail;

    private boolean gameCreatedByWeb;

    private boolean gameEnrollmentResultByEmail;

    private boolean gameEnrollmentResultByWeb;

    private boolean gameUpdatedByEmail;

    private boolean gameUpdatedByWeb;

}
