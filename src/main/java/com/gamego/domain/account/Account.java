package com.gamego.domain.account;


import com.gamego.domain.Game;
import com.gamego.domain.Time;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Setter @EqualsAndHashCode(of = "id")
@Getter @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    private boolean gameCreatedByEmail;

    private boolean gameCreatedByWeb;

    private boolean gameEnrollmentResultByEmail;

    private boolean gameEnrollmentResultByWeb;

    private boolean gameUpdatedByEmail;

    private boolean gameUpdatedByWeb;

    private LocalDateTime emailCheckTokenGeneratedAt;

    @Enumerated(EnumType.STRING)
    private Gender Gender;

    @Enumerated(EnumType.STRING)
    private AccountRole accountRole = AccountRole.USER;

    @ManyToMany
    private Set<Game> games = new HashSet<>();

    @ManyToMany
    private Set<Time> times = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this. emailCheckToken.equals(token);
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean canSendEmailAgain() {
        return this.emailCheckTokenGeneratedAt == null || this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

}
