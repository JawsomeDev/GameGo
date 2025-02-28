package com.gamego.domain.enroll;

public class EnrollRejectedEvent extends EnrollEvent{

    public EnrollRejectedEvent(Enroll enroll) {
        super(enroll, "죄송합니다. 파티 참가 신청이 거절되었습니다.");
    }
}
