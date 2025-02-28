package com.gamego.domain.enroll;


public class EnrollAcceptedEvent extends EnrollEvent{

    public EnrollAcceptedEvent(Enroll enroll) {
        super(enroll, "파티장이 참가 신청을 수락하였습니다. 파티를 확인하세요.");
    }
}
