package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.game.QGame;
import com.gamego.domain.room.QRoom;
import com.gamego.domain.room.Room;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class RoomRepositoryImpl implements RoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public RoomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Room> findByKeyword(String keyword, Account account, Pageable pageable) {
        QRoom room = QRoom.room;
        QGame game = QGame.game;

        BooleanExpression keywordCondition =
                room.title.containsIgnoreCase(keyword)
                        .or(room.games.any().name.containsIgnoreCase(keyword));


        BooleanExpression timePreferenceCondition =
                account.getTimePreference() == null
                        ? Expressions.TRUE
                        : room.timePreference.eq(account.getTimePreference());

        BooleanExpression predicate = room.active.isTrue()
                .and(keywordCondition)
                .and(timePreferenceCondition);

        JPAQuery<Room> query = queryFactory
                .selectFrom(room)
                .leftJoin(room.games, game).fetchJoin()
                .leftJoin(room.roomAccounts).fetchJoin()
                .where(predicate)
                .distinct();

        for (Sort.Order order : pageable.getSort()) {
            if (order.getProperty().equals("activeDateTime")) {
                query.orderBy(order.isAscending()
                        ? room.activeDateTime.asc()
                        : room.activeDateTime.desc());
            }
            else if (order.getProperty().equals("memberCount")) {
                query.orderBy(order.isAscending()
                        ? room.memberCount.asc()
                        : room.memberCount.desc());
            }
        }

        List<Room> rooms = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        long total = queryFactory
                .select(room.count())
                .from(room)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(rooms, pageable, total);
    }

}
