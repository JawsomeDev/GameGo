package com.gamego.validator;

import com.gamego.domain.room.form.RoomForm;
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
        return RoomForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RoomForm roomForm = (RoomForm) target;
        if(roomRepository.existsByPath(roomForm.getPath())) {
            errors.rejectValue("path", "room.exists", "해당 경로값을 사용할 수 없습니다.");
        }
    }
}
