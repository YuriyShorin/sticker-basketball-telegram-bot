package org.example.configuration;

import lombok.Data;
import org.example.service.handler.GameHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class StickerBasketballConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Bean
    GameHandler gameHandler() {
        return new GameHandler();
    }
}
