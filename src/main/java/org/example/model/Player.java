package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {

    private String telegramUserName;
    private int score = 0;
    private boolean isWinner = false;

    public Player(String telegramUserName) {
        this.telegramUserName = telegramUserName;
    }

    public String getTelegramUserName() {
        return telegramUserName;
    }

    public void resetToZero() {
        score = 0;
        isWinner = false;
    }

    public void incrementScore() {
        score++;
    }
}
