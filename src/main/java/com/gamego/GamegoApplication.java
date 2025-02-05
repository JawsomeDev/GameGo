package com.gamego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GamegoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamegoApplication.class, args);
    }

}
