package ru.tgbot.controller;


import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    private String botName = "manapa_bot";
    private String token = "5982087940:AAFvhevi1Hn7YqRqsqIpJtvDqYhWBXT1X44";

    private UpdateController updateController;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @PostConstruct
    public void init() throws TelegramApiException {
        updateController.registerBot(this);
    }


    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage().getText();
        log.log(Level.DEBUG, message);
        updateController.processUpdate(update);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void sendMessageAnswer(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
