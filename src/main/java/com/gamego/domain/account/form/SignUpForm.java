package com.gamego.domain.account.form;


import com.gamego.domain.account.Gender;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpForm {

    @NotBlank(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 15, message = "닉네임은 2~15자 이내로 입력하세요.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,15}$", message = "_-를 제외한 특수기호는 사용이 불가합니다.")
    private String nickname;

    @Email
    @NotBlank(message = "이메일을 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min=8, max = 20, message = "비밀번호는 8~20자 이내어야 합니다.")
    private String password;

    @Transient // JPA에서 제외
    private String confirmPassword;


    private Gender gender;



    // 비밀번호 일치 여부
    public boolean isPasswordMatched(){
        return password != null && password.equals(confirmPassword);
    }
}
