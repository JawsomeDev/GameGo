package com.gamego.validator;

import com.gamego.domain.account.dto.PasswordReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



@Component
@RequiredArgsConstructor
public class PasswordValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordReq.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordReq passwordReq = (PasswordReq) target;
        if(!passwordReq.getNewPassword().equals(passwordReq.getNewPasswordConfirm())){
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
