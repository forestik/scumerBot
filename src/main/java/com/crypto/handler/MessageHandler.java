package com.crypto.handler;

import com.crypto.dto.MessageDto;
import com.crypto.enums.Command;
import com.crypto.messagesender.MessageSender;
import com.crypto.service.impl.CalendarService;
import com.crypto.service.impl.ClickUpService;
import com.crypto.service.impl.ExcelService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor

public class MessageHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final MessageSender messageSender;

    private final ClickUpService clickUpService;

    private final ExcelService excelService;

    private final CalendarService calendarService;

    @Override
    public void choose(Message message) {
        if (message.hasText()) {
            logger.info("Command: {}", message.getText());
            switch (Command.getCommandoEnum(message.getText())) {
                case TEAM:
                    getTeam(message);
                    break;
                case TASKS:
                    getListTask(message);
                    break;
                case TOKENS_INFO:
                    getTokensInfo(message, message.getText().substring(7));
                    break;
                case CALENDAR:
                    getEvents(message, message.getText().substring(9));
                    break;
                case EXCEL:
                    getExcelUrl(message);
                    break;
            }
        }
    }

    private void getTeam(Message message) {
        List<? extends MessageDto> messageDtoList = clickUpService.getTeams();
        logger.info("Found {} member", messageDtoList.size());
        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Person         | Email\r\n" +
                "---------------|-------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getListTask(Message message) {
        List<? extends MessageDto> messageDtoList = clickUpService.getTasks();
        logger.info("Found {} tasks", messageDtoList.size());
        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Tasks\r\n" +
                "----------------------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getTokensInfo(Message message, String query) {
        List<? extends MessageDto> messageDtoList = new ArrayList<>();
        try {
            messageDtoList = excelService.getTokensInfo(query);
            logger.info("Found {} rows for query {}", messageDtoList.size(), query);
        } catch (IOException e) {
            logger.warn("No value present for query {}", query);
            e.printStackTrace();
        }

        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Name           | Person   | Tokens Left\r\n" +
                "---------------|----------|---------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getEvents(Message message, String query) {
        List<? extends MessageDto> messageDtoList = new ArrayList<>();
        try {
            messageDtoList = calendarService.getEvents(query);
            logger.info("Found {} events for query {}", messageDtoList.size(), query);
        } catch (IOException e) {
            logger.warn("No value present for query {}", query);
            e.printStackTrace();
        }
        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Date             | Event\r\n" +
                "-----------------|------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getExcelUrl(Message message) {
        var excelUrl = excelService.getExcelUrl();

        SendMessage sendMessage = getSendMessage(message, Collections.singletonList(new MessageDto() {
            @Override
            public String getMessage() {
                return "<a href=\"" + excelUrl + "\">Crypto</a>";
            }
        }), "");
        messageSender.sendMessage(sendMessage);
    }

    private void pingAll(Message message) {
        message.getContact();
        var excelUrl = excelService.getExcelUrl();

        SendMessage sendMessage = getSendMessage(message, Collections.singletonList(new MessageDto() {
            @Override
            public String getMessage() {
                return "<a href=\"" + excelUrl + "\">Crypto</a>";
            }
        }), "");
        messageSender.sendMessage(sendMessage);
    }

    private SendMessage getSendMessage(Message message, List<? extends MessageDto> messageDtoList, String title) {
        return SendMessage.builder()
                .text(title + "\r\n" + messageDtoList.stream()
                        .map(MessageDto::getMessage)
                        .reduce((collect1, collect2) -> collect1 + "\r\n" + collect2).orElseThrow())
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
    }
}
