package com.gamego.domain.room;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.event.Event;
import com.gamego.domain.game.Game;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Room {
    
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;


    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String longDescription;



    @Enumerated(EnumType.STRING)
    private TimePreference timePreference;

    private LocalDateTime activeDateTime;
    private LocalDateTime closedDateTime;
    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;
    private boolean active;
    private boolean closed;
    private boolean useBanner;

    @Column(nullable = false)
    private int memberCount;

    @OneToMany
    private List<Event> event = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomAccount> roomAccounts = new HashSet<>();

    @ManyToMany
    private Set<Game> games = new HashSet<>();


    public boolean isJoinable(Account account){

        boolean alreadyJoined = this.roomAccounts.stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account));

        return this.isActive() && this.isRecruiting() && !alreadyJoined;
    }
}
