package com.gamego.domain.account.dto;


import com.gamego.domain.account.accountenum.AccountRole;
import com.gamego.domain.account.accountenum.Gender;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountResp {
    private String email;
    private String nickname;
    private String bio;
    private String profileImage;
    private String location;
    private String url;
    private boolean emailVerified;
    private LocalDateTime joinedAt;
    private AccountRole accountRole;
    private Gender gender;
}
