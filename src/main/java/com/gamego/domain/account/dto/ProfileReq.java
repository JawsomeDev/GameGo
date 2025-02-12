package com.gamego.domain.account.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Data
public class ProfileReq {

    @Length(max=30)
    private String bio;

    @Length(max = 50)
    @URL
    private String url;

    @Length(max = 30)
    private String location;

    private String profileImage;
}
