package com.gamego.repository.account;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.QAccount;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.room.Room;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Account> findByGamesAndTimes(Room room) {
        QAccount account = QAccount.account;

        // 게임 조건: room의 게임 태그 중 하나라도 account에 포함되어야 함
        BooleanExpression gameCondition = account.games.any().in(room.getGames());

        TimePreference roomTime = room.getTimePreference();
        BooleanExpression timeCondition = null;
        if (roomTime != null) {
            timeCondition = account.timePreference.isNull()
                    .or(account.timePreference.eq(roomTime));
        }

        return queryFactory
                .selectDistinct(account)
                .from(account)
                .where(gameCondition, timeCondition) // timeCondition이 null이면 무시됨
                .fetch();
    }
}
