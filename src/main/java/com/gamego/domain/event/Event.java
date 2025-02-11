package com.gamego.domain.event;

import com.gamego.domain.account.Account;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account createBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;



}
