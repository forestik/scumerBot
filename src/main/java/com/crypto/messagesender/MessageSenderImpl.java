package com.crypto.messagesender;

import com.crypto.TelegramBot;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageSenderImpl implements MessageSender {

    @Lazy
    private final TelegramBot telegramBot;

    public MessageSenderImpl(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


    @Override
    public void sendMessage(SendMessage sendMessage) {
        List<SendMessage> sendMessageList = new ArrayList<>();
        if (sendMessage.getText().length() > 4096) {
            List<String> collect = Arrays.stream(sendMessage.getText()
                    .split(System.lineSeparator())).collect(Collectors.toList());
            int i1 = collect.size() / 10;
            for (int i = 0; i < collect.size(); i = i + 10) {
                String s;
                if (i != i1 * 10) {
                    s = collect.subList(i, i + 9).stream()
                            .reduce((collect1, collect2) -> collect1 + "\r\n" + collect2).orElseThrow();
                } else {
                    s = collect.subList(i, i + collect.size() % 10).stream()
                            .reduce((collect1, collect2) -> collect1 + "\r\n" + collect2).orElseThrow();
                }
                sendMessageList.add(SendMessage.builder()
                        .text(s)
                        .parseMode("HTML")
                        .chatId(sendMessage.getChatId())
                        .build());
            }
        } else {
            sendMessageList.add(sendMessage);
        }
        sendMessageList.forEach(send -> {
            try {
                telegramBot.execute(send);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }
}
