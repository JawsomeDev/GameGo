package com.gamego.repository.account;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.QAccount;
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

        BooleanExpression gameCondition = account.games.any().in(room.getGames());

        BooleanExpression timeCondition = null;
        if (room.getTimePreference() != null) {
            timeCondition = account.timePreference.isNull()
                    .or(account.timePreference.eq(room.getTimePreference()));
        }
        return queryFactory
                .selectDistinct(account)
                .from(account)
                .where(gameCondition, timeCondition)
                .fetch();

    }
}
