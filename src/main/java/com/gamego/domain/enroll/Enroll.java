package com.gamego.domain.enroll;

import com.gamego.domain.account.Account;
import com.gamego.domain.event.Event;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity  @AllArgsConstructor
@NoArgsConstructor @Builder
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

    public void addEvent(Event event) {
        this.event = event;
    }

    public void removeEvent(Event event) {
        this.event = null;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
