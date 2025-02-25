package com.gamego.domain.roomaccount;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class RoomAccount {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private RoomRole role; // 방장, 매니저, 일반

    private LocalDateTime joinedAt;

    public RoomAccount(Room room, String nickname, Account account, RoomRole role, LocalDateTime now) {
        this.room = room;
        this.nickname = nickname;
        this.account = account;
        this.role = role;
        this.joinedAt = now;
    }



    public boolean isMember() {
        return this.role == RoomRole.MEMBER;
    }

    public boolean isManager() {
        return this.role == RoomRole.MANAGER;
    }

    public boolean isMaster() {
        return this.role == RoomRole.MASTER;
    }

    public boolean isManagerOrMaster() {
        return this.role == RoomRole.MANAGER || this.role == RoomRole.MASTER;
    }

    public void promoteMember(){
        if(this.role == RoomRole.MEMBER){
            this.role = RoomRole.MANAGER;
        }
    }
    public void demoteMember(){
        if(this.role == RoomRole.MANAGER){
            this.role = RoomRole.MEMBER;
        }
    }
}

