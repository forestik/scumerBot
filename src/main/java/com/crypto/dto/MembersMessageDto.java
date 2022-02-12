package com.crypto.dto;

public class MembersMessageDto extends MessageDto {

    public MembersMessageDto(String userName, String email) {
        setMessage(userName, email);
    }

    public void setMessage(String userName, String email) {
        this.message = "<pre>" + userName + " | " + email + "</pre>";
    }
}
