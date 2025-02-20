package com.gamego.domain.enroll;

import com.gamego.domain.account.Account;
import com.gamego.domain.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity  @AllArgsConstructor
@NoArgsConstructor
@Getter @EqualsAndHashCode(of = "id")
public class Enroll {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;
}
