package com.gamego.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class BannedMemberJoinException extends RuntimeException {
    public BannedMemberJoinException(String message) {
        super(message);
    }

    public BannedMemberJoinException(){
        super("추방된 회원은 재가입할 수 없습니다.");
    }
}
