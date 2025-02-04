package com.gamego.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class GameService {

    private static final String API_URL = "https://steamspy.com/api.php?request=all";


    @PostConstruct
    public List<String> getTop1000Games() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> gameList = new ArrayList<>();

        try {
            JsonNode response = restTemplate.getForObject(API_URL, JsonNode.class);
            if (response != null) {
                response.fields().forEachRemaining(entry -> {
                    JsonNode game = entry.getValue();
                    gameList.add(game.get("name").asText());
                });
            }
        } catch (Exception e) {
            System.out.println("API 호출 실패: " + e.getMessage());
        }

        List<String> koreanGames = List.of(
                "서든어택", "카트라이더 드리프트", "던전앤파이터", "로스트아크",
                "메이플스토리", "블레이드 & 소울", "검은사막", "에오스 레드",
                "바람의나라", "뮤 온라인", "리니지", "리니지2", "엘소드", "발로란트", "오버워치2", "메이플랜드"
        );

        return gameList;
    }
}
