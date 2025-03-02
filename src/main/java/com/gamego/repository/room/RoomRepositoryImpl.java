package com.gamego.repository.room;

import com.gamego.domain.account.Account;
import com.gamego.domain.game.QGame;
import com.gamego.domain.room.QRoom;
import com.gamego.domain.room.Room;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        // 키워드 관련 조건 (방 제목 또는 게임 이름에 포함)
        BooleanExpression keywordCondition =
                room.title.containsIgnoreCase(keyword)
                        .or(room.games.any().name.containsIgnoreCase(keyword));

        // account의 timePreference가 null이면 상관 x , 아니면 매치
        BooleanExpression timePreferenceCondition =
                account.getTimePreference() == null
                        ? Expressions.TRUE : room.timePreference.eq(account.getTimePreference());

        BooleanExpression predicate = room.active.isTrue()
                .and(keywordCondition)
                .and(timePreferenceCondition);

        List<Room> rooms = queryFactory
                .selectFrom(room)
                .leftJoin(room.games, game).fetchJoin()
                .leftJoin(room.roomAccounts).fetchJoin()
                .where(predicate)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(room)
                .where(predicate)
                .fetchCount();

        return new PageImpl<>(rooms, pageable, total);
    }
}
