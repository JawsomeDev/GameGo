package com.gamego.service.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.accountenum.TimePreference;
import com.gamego.domain.game.Game;
import com.gamego.domain.game.form.GameListResp;
import com.gamego.repository.AccountRepository;
import com.gamego.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountQueryService {

    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if(account == null) {
            throw new IllegalArgumentException(account + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

    public GameListResp getGameListResponse(Account account) throws JsonProcessingException {
        Account accountWithGames = accountRepository.findAccountWithGamesById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        List<String> gameNames = accountWithGames.getGames()
                .stream()
                .map(Game::getName)
                .collect(Collectors.toList());

        List<String> allGames = gameRepository.findAll()
                .stream()
                .map(Game::getName)
                .collect(Collectors.toList());

        GameListResp response = new GameListResp();
        response.setGames(gameNames);
        response.setWhitelist(objectMapper.writeValueAsString(allGames));
        return response;
    }

    public String getTimePreference(Account account){
        TimePreference timePreference = accountRepository.findById(account.getId())
                .map(Account::getTimePreference).orElse(null);
        return timePreference != null ? timePreference.getValue() : null;
    }
}
