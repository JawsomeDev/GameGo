package com.gamego.domain.event;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.enroll.Enroll;
import com.gamego.domain.event.eventenum.EventType;
import com.gamego.domain.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(nullable = false)
    private Integer limitOfNumbers;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enroll> enrolls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;


    
    /*
        참가 가능 여부
        1. 이미 참여하면 x 
        2. 아직 모집중
        3. 이미 게임 시작했는지 (근데 이건 아직 모집중인걸로 커버가능할듯)
        4. 등록햇는지
       
     */
    public boolean isEnrollAble(AccountUserDetails accountUserDetails) {
        return !isAlreadyEnrolled(accountUserDetails) && isNotClose() && !isAttended(accountUserDetails);
    }

    public boolean isDisEnrollAble(AccountUserDetails accountUserDetails) {
        return isNotClose() && isAlreadyEnrolled(accountUserDetails);
    }

    public boolean isAlreadyEnrolled(AccountUserDetails accountUserDetails) {
        Account account = accountUserDetails.getAccount();

        return enrolls.stream()
                .anyMatch(enroll -> enroll.getAccount().equals(account));
    }
    private boolean isAttended(AccountUserDetails accountUserDetails) {
        Account account = accountUserDetails.getAccount();

        return enrolls.stream()
                .anyMatch(enroll -> enroll.getAccount().equals(account) && enroll.isAttended());
    }

    private boolean isNotClose(){
        return this.endEnrolledAt.isAfter(LocalDateTime.now());
    }

    public boolean isAbleToAcceptWaitingEnroll(){
        return this.eventType == EventType.FCFS && this.limitOfNumbers > this.getNumbersOfAcceptedEnrollments();
    }

    public Long getNumbersOfAcceptedEnrollments(){
        return this.enrolls.stream().filter(Enroll::isAccepted).count();
    }

    public Long getNumbersOfRemainingEnrollments(){
        return this.limitOfNumbers - getNumbersOfAcceptedEnrollments();
    }


    public boolean isAcceptable(Enroll enroll) {
        return this.eventType == EventType.APPROVAL
                && !enroll.isAttended() && !enroll.isAccepted();
    }

    public boolean isRejectable(Enroll enroll) {
        return this.eventType== EventType.APPROVAL
                && !enroll.isAttended() && enroll.isAccepted();
    }

    public void addEnroll(Enroll enroll) {
        this.enrolls.add(enroll);
        enroll.addEvent(this);
    }

    public void removeEnroll(Enroll enroll) {
        this.enrolls.remove(enroll);
        enroll.removeEvent(null);
    }

    public void acceptNextWaitingEnroll() {
        if(this.isAbleToAcceptWaitingEnroll()){
            Enroll enrollToAccept = this.getTheFirstWaitingEnroll();
            if(enrollToAccept != null){
                enrollToAccept.setAccepted(true);
            }
        }
    }

    public Enroll getTheFirstWaitingEnroll() {
        return enrolls.stream()
                .filter(enroll -> !enroll.isAccepted()).min(Comparator.comparing(Enroll::getEnrolledAt))
                .orElse(null);
    }

    public void accept(Enroll enroll) {
        if(this.eventType == EventType.APPROVAL && this.limitOfNumbers > this.getNumbersOfAcceptedEnrollments()){
            enroll.setAccepted(true);
        }
    }

    public void reject(Enroll enroll) {
        if(this.eventType == EventType.APPROVAL){
            enroll.setAccepted(false);
        }
    }
}
