package com.gamego.repository;

import com.gamego.domain.roomaccount.BanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanHistoryRepository extends JpaRepository<BanHistory, Long> {
    boolean existsByRoomIdAndAccountId(Long roomId, Long accountId);
}
