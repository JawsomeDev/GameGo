package com.gamego.repository.account;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.room.Room;

import java.util.List;
import java.util.Set;

public interface AccountRepositoryCustom {

    List<Account> findByGamesAndTimes(Room room);
}
