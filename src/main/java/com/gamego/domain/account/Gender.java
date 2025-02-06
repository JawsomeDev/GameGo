package com.gamego.domain.account;

import lombok.Getter;

@Getter
public enum Gender {
    male("남"), female("여");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

}
