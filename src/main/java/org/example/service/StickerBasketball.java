package org.example.service;

import org.example.configuration.StickerBasketballConfig;
import org.example.service.handler.GameHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class StickerBasketball extends TelegramLongPollingBot {

    private final StickerBasketballConfig config;

    private final GameHandler gameHandler;

    @Autowired
    StickerBasketball(StickerBasketballConfig config, GameHandler gameHandler) {
        this.config = config;
        this.gameHandler = gameHandler;
        this.gameHandler.setBot(this);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasDice() && gameHandler.isGameFinished()) {
            gameHandler.throwingBallAfterGame(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasDice() && gameHandler.isGameStarted()) {
            gameHandler.processGame(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            checkCommand(update);
        }
    }

    private void checkCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        if (!messageText.startsWith("/")) {
            return;
        }
        switch (messageText) {
            case "/start" -> gameHandler.startCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
            case "/startGame" -> gameHandler.startGameCommandReceived(chatId);
            case "/play" -> gameHandler.playCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
            case "/clear" -> gameHandler.clearCommandReceived(chatId);
            default -> { }
        }
    }

    public void sendMessage(long charId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(charId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }
}

