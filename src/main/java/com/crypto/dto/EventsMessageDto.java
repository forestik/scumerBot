package com.crypto.dto;

public class EventsMessageDto extends MessageDto {

    public EventsMessageDto(String name, String date) {
        setMessage(name, date);
    }

    public void setMessage(String name, String date) {
        message = "<pre>" + date + " | " + name + "</pre>";
    }

}
