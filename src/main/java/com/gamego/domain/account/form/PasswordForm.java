package com.gamego.domain.account.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordForm {

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min=8, max = 20, message = "비밀번호는 8~20자 이내어야 합니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min=8, max = 20, message = "비밀번호는 8~20자 이내어야 합니다.")
    private String newPasswordConfirm;
}
