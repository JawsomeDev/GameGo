package com.gamego.domain.roomaccount;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder @Getter
@Entity @NoArgsConstructor @AllArgsConstructor
public class BanHistory {

    @Id @GeneratedValue
    private Long id;

    private Long roomId;

    private Long accountId;

    private LocalDateTime bannedAt;
}
