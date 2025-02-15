package com.gamego.validator;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.dto.NicknameForm;
import com.gamego.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository  accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm form = (NicknameForm) target;
        Account account = accountRepository.findByNickname(form.getNickname());
        if(account !=null){
            errors.rejectValue("nickname", "wrong.value", "이미 사용중인 닉네임입니다.");
        }
    }
}
