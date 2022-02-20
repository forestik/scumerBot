package com.crypto.dto;

public class PrincipalsMessageDto extends MessageDto{

    public PrincipalsMessageDto(String name) {
        setMessage(name);
    }

    public void setMessage(String name) {
        message = name;
    }
}
