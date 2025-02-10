package com.gamego.domain.account.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.parameters.P;

@Data
public class NicknameForm {

    @NotBlank
    @Length(min=2, max = 15)
    @Pattern(regexp =  "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,15}$", message = "_-를 제외한 특수기호는 사용이 불가합니다.")
    private String nickname;
}
