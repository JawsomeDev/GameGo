package com.gamego.domain.account.accountenum;

import lombok.Getter;

@Getter
public enum Gender {
    male("남"), female("여");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

}
