package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.QGame;
import com.gamego.domain.review.QReview;
import com.gamego.domain.room.QRoom;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.QRoomAccount;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
                .where(predicate)
                .leftJoin(room.games, QGame.game).fetchJoin()
                .leftJoin(room.roomAccounts, QRoomAccount.roomAccount).fetchJoin()
                .leftJoin(room.reviews, QReview.review).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct();

        // 여러 정렬 방식
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

        List<Room> rooms = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(room.count())
                .from(room)
                .where(predicate);

        return PageableExecutionUtils.getPage(rooms, pageable, countQuery::fetchOne);
         }
    @Override
    public List<Room> findByAccount(Set<Game> games, TimePreference timePreference) {
        QRoom room = QRoom.room;
        return queryFactory.selectFrom(room)
                .where(
                        room.active.isTrue(),
                        room.closed.isFalse(),
                        room.games.any().in(games),
                        timePreference != null ? room.timePreference.eq(timePreference) : null
                )
                .leftJoin(room.games, QGame.game).fetchJoin()
                .leftJoin(room.roomAccounts, QRoomAccount.roomAccount).fetchJoin()
                .leftJoin(room.reviews, QReview.review).fetchJoin()
                .orderBy(room.activeDateTime.desc())
                .distinct()
                .limit(9)
                .fetch();
    }

}
