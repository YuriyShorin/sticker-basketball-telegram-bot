package org.example.service.handler;

import org.example.model.Player;
import org.example.service.StickerBasketball;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;


public class GameHandler {

    private final HashSet<String> playersSet = new HashSet<>();
    private final ArrayList<Player> playersTable = new ArrayList<>();
    private final ArrayList<String> playersList = new ArrayList<>();
    private boolean isGameStarted = false;
    private boolean isGameFinished = false;
    private int round = 1;
    private int indexOfCurrentPlayer = 0;
    private StickerBasketball bot;

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public void setBot(StickerBasketball bot) {
        this.bot = bot;
    }

    public void startCommandReceived(long chatId, String username) {
        bot.sendMessage(chatId, "Привет, " + username + "!");
    }

    public void playCommandReceived(long chatId, String username) {
        if (isGameStarted) {
            bot.sendMessage(chatId, "Ты можешь зарегестрироваться после окончания игры");
            return;
        }
        if (playersSet.contains(username)) {
            bot.sendMessage(chatId, "Ты уже зарегестрирован");
            return;
        }
        playersSet.add(username);
        playersList.add(username);
        bot.sendMessage(chatId, username + " зарегистрирован!");
    }

    public void startGameCommandReceived(long chatId) {
        if (isGameStarted) {
            bot.sendMessage(chatId, "Игра уже началась");
            return;
        }
        if (playersList.size() == 2 || playersList.size() == 4 || playersList.size() == 8) {
            isGameStarted = true;
            isGameFinished = false;
            Random random = new Random();
            int numberOfNotAddedPlayers = playersList.size();
            int playersNumber = playersList.size();
            for (int i = 0; i < playersNumber; ++i) {
                int index = random.nextInt(numberOfNotAddedPlayers--);
                playersTable.add(new Player(playersList.get(index)));
                playersList.remove(index);
            }
            createTable(chatId);
            bot.sendMessage(chatId, "Играют:\n " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " vs " + playersTable.get(indexOfCurrentPlayer+1));
            playersSet.clear();
            playersList.clear();
        } else {
            bot.sendMessage(chatId, "Можно играть только в 2, 4 или 8 человек :(");
        }
    }

    public void clearCommandReceived(long chatId) {
        if (isGameStarted) {
            bot.sendMessage(chatId, "Нельзя очистить участников, когда идет игра");
            return;
        }
        playersList.clear();
        playersSet.clear();
        playersTable.clear();
        isGameFinished = false;
        indexOfCurrentPlayer = 0;
        round = 1;
    }

    public void throwingBallAfterGame(Update update) {
        long chatId = update.getMessage().getChatId();
        bot.sendMessage(chatId, "Игра закончена, зачем ты бросаешь?");
    }

    public void processGame(Update update) {
        long chatId = update.getMessage().getChatId();
        if (!update.getMessage().getFrom().getFirstName().equals(playersTable.get(indexOfCurrentPlayer).getTelegramUserName())) {
            bot.sendMessage(chatId, "Бросает: " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName());
            return;
        }
        int ballValue = update.getMessage().getDice().getValue();
        processBallThrow(chatId, ballValue);
        IsGoal(chatId, ballValue);
        if (isWin(chatId)) {
            return;
        }
        changeIndexOfCurrentPlayer();
    }

    private void createTable(long chatId) {
        StringBuilder tableToSend = new StringBuilder();
        tableToSend.append("Раунд №").append(round).append(":\n\n");
        for (int i = 0; i < playersTable.size(); ++i) {
            tableToSend.append(playersTable.get(i).getTelegramUserName()).append("\n");
            if (i % 2 == 1) {
                tableToSend.append("\n");
            }
        }
        bot.sendMessage(chatId, tableToSend.toString());
    }

    private void processBallThrow(long chatId, int ballValue) {
        switch (ballValue) {
            case 1 -> answerToThrow(chatId, 4200, "Мимо");
            case 2 -> answerToThrow(chatId, 4200, "Почти попал");
            case 3 -> answerToThrow(chatId, 4200, "Доставай");
            case 4 -> answerToThrow(chatId, 3000, "Фонарь");
            case 5 -> answerToThrow(chatId, 4200, "Чистый");
        }
    }

    private void answerToThrow(long chatId, int timeToSleep, String answerToThrow) {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        bot.sendMessage(chatId, answerToThrow);
    }

    private void IsGoal(long chatId, int ballValue) {
        if (ballValue > 3) {
            playersTable.get(indexOfCurrentPlayer).incrementScore();
            if (indexOfCurrentPlayer % 2 == 0) {
                bot.sendMessage(chatId, playersTable.get(indexOfCurrentPlayer).getScore() + ":" + playersTable.get(indexOfCurrentPlayer + 1).getScore());
            } else {
                bot.sendMessage(chatId, playersTable.get(indexOfCurrentPlayer - 1).getScore() + ":" + playersTable.get(indexOfCurrentPlayer).getScore());
            }
        }
    }

    private boolean isWin(long chatId) {
        if (indexOfCurrentPlayer % 2 == 1) {
            if (playersTable.get(indexOfCurrentPlayer).getScore() < 3 && playersTable.get(indexOfCurrentPlayer - 1).getScore() < 3) {
                return false;
            }
            if (playersTable.get(indexOfCurrentPlayer).getScore() > playersTable.get(indexOfCurrentPlayer - 1).getScore()) {
                playersTable.get(indexOfCurrentPlayer).setWinner(true);
                bot.sendMessage(chatId, playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " победил");
                indexOfCurrentPlayer++;
                if (!isRoundFinished(chatId)) {
                    bot.sendMessage(chatId, "Играют:\n " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " vs " + playersTable.get(indexOfCurrentPlayer+1));
                }
                return true;
            }
            if (playersTable.get(indexOfCurrentPlayer).getScore() < playersTable.get(indexOfCurrentPlayer - 1).getScore()) {
                playersTable.get(indexOfCurrentPlayer - 1).setWinner(true);
                bot.sendMessage(chatId, playersTable.get(indexOfCurrentPlayer - 1).getTelegramUserName() + " победил");
                indexOfCurrentPlayer++;
                if (!isRoundFinished(chatId)) {
                    bot.sendMessage(chatId, "Играют:\n " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " vs " + playersTable.get(indexOfCurrentPlayer+1));
                }
                return true;
            }
        }
        return false;
    }

    private boolean isRoundFinished(long chatId) {
        if (indexOfCurrentPlayer >= playersTable.size()) {
            bot.sendMessage(chatId, "Раунд закончен");
            if (isGameFinished(chatId)) {
                return true;
            }
            newRound(chatId);
            return true;
        }
        return false;
    }

    private void newRound(long chatId) {
        round++;
        indexOfCurrentPlayer = 0;
        int playersTableSize = playersTable.size();
        for (int i = 0; i < playersTableSize; ++i) {
            if (!playersTable.get(i).isWinner()) {
                playersTable.remove(i);
                i--;
            } else {
                playersTable.get(i).resetToZero();
            }
            if (i == playersTable.size() - 1) {
                break;
            }
        }
        bot.sendMessage(chatId, "Играют:\n " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " vs " + playersTable.get(indexOfCurrentPlayer+1));
        createTable(chatId);
    }

    private boolean isGameFinished(long chatId) {
        if (playersTable.size() == 2) {
            if (playersTable.get(0).isWinner()) {
                bot.sendMessage(chatId, playersTable.get(0).getTelegramUserName() + " чемпион!!!");
            } else {
                bot.sendMessage(chatId, playersTable.get(1).getTelegramUserName() + " чемпион!!!");
            }
            isGameStarted = false;
            clearCommandReceived(chatId);
            return true;
        }
        return false;
    }

    private void changeIndexOfCurrentPlayer() {
        if (indexOfCurrentPlayer % 2 == 0) {
            indexOfCurrentPlayer++;
        } else {
            indexOfCurrentPlayer--;
        }
    }
}

