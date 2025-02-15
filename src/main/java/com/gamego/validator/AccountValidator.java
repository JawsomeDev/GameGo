package com.gamego.validator;


import com.gamego.domain.account.dto.AccountForm;
import com.gamego.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class AccountValidator implements Validator {

    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return AccountForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountForm accountForm = (AccountForm) target;

        //비번 확인
        if(!accountForm.isPasswordMatched()) {
            errors.rejectValue("confirmPassword", "password.not.match", "비밀번호가 일치하지 않습니다.");
        }

        // 중복 체크
        if(accountRepository.existsByEmail(accountForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{accountForm.getEmail()}, "이미 사용중인 이메일 입니다.");
        }
        if(accountRepository.existsByNickname(accountForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{accountForm.getNickname()}, "이미 사용중인 닉네임 입니다.");
        }

        if(accountForm.getGender() == null){
            errors.rejectValue("gender", "invalid.gender", "성별을 선택하세요.");
        }
    }
}
