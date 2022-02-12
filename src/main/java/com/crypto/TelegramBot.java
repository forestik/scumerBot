package com.crypto;

import com.crypto.handler.Handler;
import com.crypto.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final String botUserName;

    private final String token;

    private final Handler handler;

    public TelegramBot(@Value("${telegram.bot.userName}") String botUserName,
                       @Value("${telegram.bot.token}") String token,
                       Handler handler) {
        this.botUserName = botUserName;
        this.token = token;
        this.handler = handler;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().getText().contains("/")) {
            handler.choose(update.getMessage());
        }
    }

    @Scheduled(cron = "0 50 8 * * 3")
    public void testScheduled() throws TelegramApiException {
        logger.info("Scheduled meeting");
        SendMessage build = SendMessage.builder()
                .text("<b>Hey sCUMers ⊙_⊙\r\n" +
                        "We have a meeting scheduled for 9:00<b>")
                .parseMode("HTML")
                .chatId("-1001586818258")
                .build();
        this.execute(build);
    }
}
