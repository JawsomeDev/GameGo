package com.gamego.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamego.domain.Game;
import com.gamego.repository.GameRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j

@RequiredArgsConstructor
@Service
public class GameService {

    private static final String API_URL = "https://steamspy.com/api.php?request=top100forever";
    private final GameRepository gameRepository;


    @Transactional
    @EventListener(ContextRefreshedEvent.class)
    public void getTop1000Games() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> gameList = new ArrayList<>();

        try {
            JsonNode response = restTemplate.getForObject(API_URL, JsonNode.class);
            if (response != null) {
                response.fields().forEachRemaining(entry -> {
                    JsonNode game = entry.getValue();
                    gameList.add(game.get("name").asText());
                    log.info("API 호출 성공 : {}", game.get("name").asText());
                });
            }
        } catch (Exception e) {
            log.info("API 호출 실패 : {}", e.getMessage());
        }
        List<String> koreaGames = List.of("리그오브레전드",
                "서든어택", "카트라이더 드리프트", "던전앤파이터", "로스트아크",
                "메이플스토리", "블레이드 & 소울", "검은사막", "에오스 레드",
                "바람의나라", "뮤 온라인", "리니지", "리니지2", "엘소드", "발로란트", "오버워치2", "메이플랜드"
        );
        gameList.addAll(koreaGames);

        // 🛠️ 게임 리스트 -> Game 엔티티로 변환 후 DB 저장
        List<Game> games = gameList.stream()
                .map(name -> Game.builder().name(name).build())
                .toList();

        for (Game game : games) {
            if(!gameRepository.existsByName(game.getName())) {
                log.info("게임 저장 :  {}", game.getName());
                gameRepository.save(game);
            }else{
                log.info("이미 존재하는 게임 : {}", game.getName());
            }
        }
    }
}
