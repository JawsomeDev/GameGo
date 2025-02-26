package com.gamego.domain.review;


import com.gamego.domain.account.Account;
import com.gamego.domain.room.Room;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private String content;

    @Column(nullable = false)
    private Integer rating; // 별점 1~5

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeRating(Integer rating) {
        this.rating = rating;
    }
}
