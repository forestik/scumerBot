package com.crypto.service.impl;

import com.crypto.dto.EventsMessageDto;
import com.crypto.service.DefaultService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
public class CalendarService implements DefaultService {

    private final Calendar calendar;

    private final String calendarId;

    public CalendarService(Calendar calendar, @Value("${calendar.calendarId}") String calendarId) {
        this.calendar = calendar;
        this.calendarId = calendarId;
    }

    public List<EventsMessageDto> getEvents(String query) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = calendar.events().list(calendarId)
                .setMaxResults(query.isEmpty() ? 10 : Integer.parseInt(query))
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        List<EventsMessageDto> eventsMessageDtoList = new LinkedList<>();
        items.forEach(item -> eventsMessageDtoList
                .add((new EventsMessageDto(item.getSummary(),
                        getEnrichedString(item.getStart().getDateTime().toString(), 16)))));
        return eventsMessageDtoList;
    }
}
