package com.gamego.validator;

import com.gamego.domain.room.dto.RoomReq;
import com.gamego.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
@RequiredArgsConstructor
public class RoomValidator implements Validator {

    private final RoomRepository roomRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return RoomReq.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RoomReq roomReq = (RoomReq) target;
        if(roomRepository.existsByPath(roomReq.getPath())) {
            errors.rejectValue("path", "room.exists", "해당 경로값을 사용할 수 없습니다.");
        }
    }
}
