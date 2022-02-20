package com.crypto.handler;

import com.crypto.dto.MessageDto;
import com.crypto.enums.Command;
import com.crypto.messagesender.MessageSender;
import com.crypto.service.impl.CalendarService;
import com.crypto.service.impl.ClickUpService;
import com.crypto.service.impl.ExcelService;
import com.crypto.service.impl.PrincipalService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MessageHandler implements Handler {

    private final MessageSender messageSender;

    private final ClickUpService clickUpService;

    private final ExcelService excelService;

    private final CalendarService calendarService;

    private final PrincipalService principalService;

    @Override
    public void choose(Message message) {
        if (message.hasText()) {
            log.info("Command: {}", message.getText());
            switch (Command.getCommandoEnum(message.getText())) {
                default:
                    addPrincipals(message.getFrom());
                case TEAM:
                    getTeam(message);
                    break;
                case TASKS:
                    getListTask(message);
                    break;
                case TOKENS_INFO:
                    getTokensInfo(message, message.getText().contains("@")
                            ? ""
                            : message.getText().substring(7));
                    break;
                case CALENDAR:
                    getEvents(message, message.getText().contains("@")
                            ? ""
                            : message.getText().substring(9));
                    break;
                case EXCEL:
                    getExcelUrl(message);
                    break;
                case TAG_IN:
                    tagIn(message);
                    break;
                case TAG_ALL:
                    tagAll(message);
                    break;
                case RATING:
                    getRating(message);
                    break;
                case ADD_RATING :
                    addRating(message);
                    break;
                case LOWER_RATING:
                    lowerRating(message);
                    break;
            }
        }
    }

    private void getTeam(Message message) {
        List<? extends MessageDto> messageDtoList = clickUpService.getTeams();
        log.info("Found {} member", messageDtoList.size());
        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Person         | Email\r\n" +
                "---------------|-------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getListTask(Message message) {
        List<? extends MessageDto> messageDtoList = clickUpService.getTasks();
        log.info("Found {} tasks", messageDtoList.size());
        SendMessage sendMessage = getSendMessage(message, messageDtoList, "<pre>Tasks\r\n" +
                "----------------------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void getTokensInfo(Message message, String query) {
        List<? extends MessageDto> messageDtoList = new ArrayList<>();
        try {
            messageDtoList = excelService.getTokensInfo(query);
            log.info("Found {} rows for query {}", messageDtoList.size(), query);
        } catch (IOException e) {
            log.warn("No value present for query {}", query);
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
            log.info("Found {} events for query {}", messageDtoList.size(), query);
        } catch (IOException e) {
            log.warn("No value present for query {}", query);
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

    private void tagIn(Message message){
        String title = principalService.tagIn(message.getFrom());

        SendMessage sendMessage = SendMessage.builder()
                .text("<pre>" + title + "</pre> " + "@" + message.getFrom().getUserName())
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void tagAll(Message message) {
        var principalsMessageDtoList = principalService.tagAll();

        SendMessage sendMessage = getSendMessage(message, principalsMessageDtoList, "<pre>Hey scumers \r\n" +
                "----------------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }



    private void addRating(Message message){
        addPrincipals(message.getReplyToMessage().getFrom());
        String title = principalService.addRating(message.getReplyToMessage().getFrom().getUserName());
        SendMessage sendMessage = SendMessage.builder()
                .text(title)
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void lowerRating(Message message){
        addPrincipals(message.getReplyToMessage().getFrom());
        String title = principalService.lowerRating(message.getReplyToMessage().getFrom().getUserName());
        SendMessage sendMessage = SendMessage.builder()
                .text(title)
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void getRating(Message message){
        List<? extends MessageDto> ratingMessageDtoList = principalService.getRating();
        log.info("Found {} member", ratingMessageDtoList.size());
        SendMessage sendMessage = getSendMessage(message, ratingMessageDtoList, "<pre>Person               | Rating\r\n" +
                "---------------------|-------------------------</pre>");
        messageSender.sendMessage(sendMessage);
    }

    private void addPrincipals(User user) {
        principalService.addPrincipal(user);
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
