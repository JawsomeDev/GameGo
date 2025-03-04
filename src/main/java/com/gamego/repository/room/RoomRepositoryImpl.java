package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.QGame;
import com.gamego.domain.review.QReview;
import com.gamego.domain.review.Review;
import com.gamego.domain.room.QRoom;
import com.gamego.domain.room.Room;
import com.gamego.domain.roomaccount.QRoomAccount;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                        room.timePreference.eq(timePreference)
                )
                .leftJoin(room.games, QGame.game).fetchJoin()
                .orderBy(room.activeDateTime.desc())
                .distinct()
                .limit(9)
                .fetch();
    }

//    @Override
//    public Map<Long, Double> findAverageRatingForRooms(List<Long> roomIds) {
//        QRoom room = QRoom.room;
//        QReview review = QReview.review;
//
//        // Room과 Review를 fetch join하여 한 번에 조회합니다.
//        List<Room> roomsWithReviews = queryFactory
//                .selectFrom(room)
//                .leftJoin(room.reviews, review).fetchJoin()
//                .leftJoin(room.games, QGame.game).fetchJoin()
//                .leftJoin(room.roomAccounts, QRoomAccount.roomAccount).fetchJoin()
//                .where(room.id.in(roomIds))
//                .distinct()
//                .fetch();
//
//        return roomsWithReviews.stream()
//                .collect(Collectors.toMap(
//                        Room::getId,
//                        r -> r.getReviews().stream()
//                                .mapToDouble(Review::getRating)
//                                .average()
//                                .orElse(0.0)
//                ));
//    }

}
