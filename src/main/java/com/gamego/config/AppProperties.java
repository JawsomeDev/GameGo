package com.gamego.config;


import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppProperties {

    private String host;
}
