package com.gamego.domain.messages;


import com.gamego.domain.account.Account;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity @Setter
@Getter @EqualsAndHashCode(of = "id")
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked=false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;



    public void changeChecked(boolean checked) {
        this.checked = checked;
    }
}
