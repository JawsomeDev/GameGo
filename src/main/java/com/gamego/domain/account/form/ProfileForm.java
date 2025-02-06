package com.gamego.domain.account.form;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ProfileForm {

    @Length(max=35)
    private String bio;

    @Length(max = 50)
    private String url;

    @Length(max = 30)
    private String location;

    private String profileImage;
}
