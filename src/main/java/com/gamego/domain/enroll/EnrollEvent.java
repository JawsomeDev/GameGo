package com.gamego.domain.enroll;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EnrollEvent {

    private final Enroll enroll;

    private final String message;
}
