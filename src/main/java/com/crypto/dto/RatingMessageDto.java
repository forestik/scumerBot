package com.crypto.dto;

public class RatingMessageDto extends MessageDto{

    public RatingMessageDto(String name, String rating) {
        setMessage(name, rating);
    }

    public void setMessage(String name, String rating) {
        message = "<pre>" + name + " | "  + rating + "</pre>";
    }
}
