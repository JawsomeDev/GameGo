package com.gamego.domain.account.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordForm {

    @NotBlank
    @Length(min=8, max = 20)
    private String newPassword;

    @NotBlank
    @Length(min=8, max = 20)
    private String newPasswordConfirm;
}
