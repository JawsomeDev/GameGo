package com.gamego.domain.event;

import com.gamego.domain.account.Account;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.eventenum.EventType;
import com.gamego.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account createBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;


    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime endEnrolledAt;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column
    private Integer limitOfNumbers;

    @OneToMany(mappedBy = "event")
    private List<Enroll> enrolls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;




    public Long getNumbersOfAcceptedEnrollments(){
        return this.enrolls.stream().filter(Enroll::isAccepted).count();
    }
}
