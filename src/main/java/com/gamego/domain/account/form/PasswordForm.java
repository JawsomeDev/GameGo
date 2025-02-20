package com.gamego.domain.account.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class PasswordForm {

    @NotBlank
    @Size(min=8, max = 20)
    private String newPassword;

    @NotBlank
    @Size(min=8, max = 20)
    private String newPasswordConfirm;
}
