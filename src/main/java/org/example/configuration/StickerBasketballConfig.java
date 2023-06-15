package org.example.configuration;

import lombok.Data;
import lombok.Getter;
import org.example.service.handler.GameHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Getter
public class StickerBasketballConfig {

    private String botName = System.getenv("BOT_NAME");

    private String botToken = System.getenv("BOT_TOKEN");

    @Bean
    GameHandler gameHandler() {
        return new GameHandler();
    }
}
