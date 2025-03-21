package com.gamego.domain.account;


import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.game.Game;
import com.gamego.domain.account.accountenum.AccountRole;
import com.gamego.domain.account.accountenum.Gender;
import com.gamego.domain.account.accountenum.TimePreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @EqualsAndHashCode(of = "id")
@Getter @AllArgsConstructor @Setter
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified = false;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String location;

    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;

    private boolean gameCreatedByEmail;

    private boolean gameCreatedByWeb = true;

    private boolean gameEnrollmentResultByEmail;

    private boolean gameEnrollmentResultByWeb = true;

    private boolean gameUpdatedByEmail;

    private boolean gameUpdatedByWeb = true;

    private LocalDateTime emailCheckTokenGeneratedAt;

    @Enumerated(EnumType.STRING)
    private TimePreference timePreference;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AccountRole accountRole;

    @ManyToMany
    private Set<Game> games = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomAccount> roomAccounts = new HashSet<>();

    private String resetPasswordToken;

    private LocalDateTime resetPasswordTokenExpiresAt;

    private LocalDateTime resetPasswordTokenGeneratedAt;

    public void generatePasswordToken(){
        this.resetPasswordToken = UUID.randomUUID().toString();
        this.resetPasswordTokenExpiresAt = LocalDateTime.now().plusHours(1);
        this.resetPasswordTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isResetPasswordTokenValid(String token) {
        return token.equals(this.resetPasswordToken)
                && resetPasswordTokenExpiresAt != null
                && LocalDateTime.now().isBefore(resetPasswordTokenExpiresAt);
    }

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.accountRole = AccountRole.USER;
        this.joinedAt = LocalDateTime.now();
    }
    public boolean canSendPasswordResetEmail(){
        return this.resetPasswordTokenGeneratedAt == null || this.resetPasswordTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean canSendEmailAgain() {
        return this.emailCheckTokenGeneratedAt == null || this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public Account(){
        this.accountRole = AccountRole.UNVERIFIED;
        this.emailVerified = false;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateTimePreference(TimePreference newPreference) {
        this.timePreference = newPreference;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }


}
