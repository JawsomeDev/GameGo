package com.gamego.validator;


import com.gamego.domain.form.SignUpForm;
import com.gamego.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return SignUpForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;

        //비번 확인
        if(!signUpForm.isPasswordMatched()) {
            errors.rejectValue("confirmPassword", "password.not.match", "비밀번호가 일치하지 않습니다.");
        }

        // 중복 체크
        if(accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일 입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임 입니다.");
        }

        if(signUpForm.getGender() == null){
            errors.rejectValue("gender", "invalid.gender", "성별을 선택하세요.");
        }
    }
}
