package com.gamego.domain.room;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.AccountUserDetails;
import com.gamego.domain.review.Review;
import com.gamego.domain.roomaccount.RoomAccount;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.event.Event;
import com.gamego.domain.game.Game;
import com.gamego.domain.roomaccount.RoomRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity @Setter
@Getter @AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room {
    
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String path;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;


    @Lob
    @Column(nullable = false)
    @Basic(fetch = FetchType.EAGER)
    private String longDescription;

    private Double reviewScore;

    @Enumerated(EnumType.STRING)
    private TimePreference timePreference;


    private LocalDateTime activeDateTime;
    private LocalDateTime closedDateTime;
    private LocalDateTime recruitingUpdatedDateTime;
    private LocalDate recruitmentChangeDate;

    private boolean recruiting;
    private boolean active;
    private boolean closed;
    private boolean useBanner=false;

    private int recruitmentChangeCountToday;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "integer default 1")
    private int memberCount;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomAccount> roomAccounts = new HashSet<>();

    @ManyToMany
    private Set<Game> games = new HashSet<>();

    @Transient
    private double averageRating;

    public void updateBanner(String image) {
        this.image = image;
    }

    public void disableBanner() {
        this.useBanner = false;
    }

    public void enableBanner() {
        this.useBanner = true;
    }

    public void defaultImage() {
        this.image = null;
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public boolean isJoinAble(AccountUserDetails accountUserDetails) {
        Account account = accountUserDetails.getAccount();
        boolean alreadyJoined = roomAccounts.stream()
                .anyMatch(roomAccount -> roomAccount.getAccount().equals(account));
        return this.isActive() && this.isRecruiting() && !alreadyJoined;
    }


    public void updateTimePreference(TimePreference timePreference) {
        this.timePreference = timePreference;
    }

    public void active() {
        if(!this.closed && !this.active){
            this.active = true;
            this.closed = false;
            this.activeDateTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("방이 이미 활성화 되어있거나 비활성화 되었습니다.");
        }
    }

    public void close(){
        if(this.active && !this.closed){
            this.active = false;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("방이 이미 비활성화되었거나 활성화 되어있지 않습니다.");
        }
    }

    // 1시간에 한번만
    public boolean canRecruitByTime() {
        if(this.recruitingUpdatedDateTime == null){
            return this.active;
        }else {
            return this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
        }
    }

    public boolean canRecruitByDay() {
        LocalDate today = LocalDate.now();
        // 오늘 변경 기록이 없거나, 기록된 날짜가 오늘이 아니라면 (즉, 오늘 처음 변경하는 경우)
        if (this.recruitmentChangeDate == null || !this.recruitmentChangeDate.equals(today)) {
            return true;
        }
        // 오늘 변경한 횟수가 3회 미만이면 변경 가능
        return this.recruitmentChangeCountToday < 3;
    }

    public void updateRecruitmentChangeTracking(){
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        if (this.recruitmentChangeDate == null || !this.recruitmentChangeDate.equals(today)) {
            this.recruitmentChangeDate = today;
            this.recruitmentChangeCountToday = 1;
        }else {
            this.recruitmentChangeCountToday++;
        }
        this.recruitingUpdatedDateTime = now;
    }

    public void changeRecruiting(boolean recruiting) {
        this.recruiting = recruiting;
    }

    public void changeRecruitmentChangeCountToday(int recruitmentChangeCountToday) {
        this.recruitmentChangeCountToday = recruitmentChangeCountToday;
    }

    public void changePath(String newPath) {
        this.path = newPath;
    }

    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void addRoomAccount(RoomAccount roomAccount) {
        this.roomAccounts.add(roomAccount);
        roomAccount.setRoom(this);
    }


    public void addMember(Account account) {
        RoomAccount roomAccount = RoomAccount.builder()
                .room(this)
                .account(account)
                .nickname(account.getNickname())
                .role(RoomRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        this.memberCount++;
        this.roomAccounts.add(roomAccount);
    }

    public void removeMember(Account account) {
        this.roomAccounts.removeIf(ra -> ra.getAccount().equals(account));
        this.memberCount--;
    }

    public void bannedMember(Account account) {
        this.memberCount--;
    }

    public void promoteMember(Account account) {
        for(RoomAccount ra : this.roomAccounts){
            if(ra.getAccount().equals(account) && ra.getRole().equals(RoomRole.MEMBER)){
                ra.promoteMember();
                break;
            }
        }
    }

    public void setReviewScore(Double reviewScore) {
        this.reviewScore = reviewScore;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void demoteMember(Account account) {
        for(RoomAccount ra : this.roomAccounts){
            if(ra.getAccount().equals(account) && ra.getRole().equals(RoomRole.MANAGER)){
                ra.demoteMember();
                break;
            }
        }
    }

    public double calculateAverageRating() {
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }
}